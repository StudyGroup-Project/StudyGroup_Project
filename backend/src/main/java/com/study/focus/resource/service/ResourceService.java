package com.study.focus.resource.service;

import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.dto.CreateResourceRequest;
import com.study.focus.resource.dto.GetResourceDetailResponse;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.dto.ResourceDetailFileDto;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.StudyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;

    // 자료 목록 가져오기
    public List<GetResourcesResponse> getResources(Long studyId, Long userId) {
        //1. 스터디 그룹 멤버 확인
        groupService.memberValidation(studyId, userId);
        //2. 데이터 조회
        List<Resource> groupResource = resourceRepository.findAllByStudy_Id(studyId);

        //3.응답 데이터 생성
        List<GetResourcesResponse> resourcesResponseList = groupResource.stream()
                .map(r -> {
                            GetMyProfileResponse userProfile = userService.getMyProfile(r.getAuthor().getUser().getId());
                            return GetResourcesResponse.builder().resourceId(r.getId())
                                    .title(r.getTitle())
                                    .userName(userProfile.getNickname())
                                    .createdAt(r.getCreatedAt()).build();
                        }
                ).toList();
        return resourcesResponseList;
    }

    // 자료 생성
    public void createResource(Long studyId, Long userId, CreateResourceRequest  resourceRequest) {
        //멤버 검증
        StudyMember studyMember = groupService.memberValidation(studyId, userId);

        //자료 생성
        Resource newResource = resourceRepository.save(
                Resource.builder().author(studyMember).study(studyMember.getStudy()).title(resourceRequest.getTitle())
                .description(resourceRequest.getContent()).build());

        //파일이 있는 경우
        if(resourceRequest.getFiles() !=null && !resourceRequest.getFiles().isEmpty())
        {
            List<MultipartFile> files = resourceRequest.getFiles();
            List<FileDetailDto> fileDetailDtoList = files.stream().map(s3Uploader::makeMetaData).toList();
            fileUploadDbAndS3(files,fileDetailDtoList,newResource);
        }
    }

    // 자료 상세 데이터 가져오기
    public GetResourceDetailResponse getResourceDetail(Long studyId, Long resourceId,Long userId) {
        //멤버 검증
        groupService.memberValidation(studyId,userId);

        //자료 찾기
        Resource resource = findResource(studyId, resourceId);
        //자료 저자 찾기
        GetMyProfileResponse authorProfile = userService.getMyProfile(resource.getAuthor().getUser().getId());
        //자료 파일 찾기
        List<ResourceDetailFileDto> resourceFiles = fileRepository.findAllByResource_Id(resourceId).stream()
                .map(f ->  new ResourceDetailFileDto(f.getFileName(), s3Uploader.getUrlFile(f.getFileKey()))).toList();

        return new GetResourceDetailResponse(resource.getTitle(),resource.getDescription(),authorProfile.getNickname()
        ,authorProfile.getProfileImageUrl(),resource.getCreatedAt(),resourceFiles);
    }



    // 자료 수정
    public void updateResource(Long studyId, Long resourceId) {
        // TODO: 자료 수정
    }

    // 자료 삭제
    public void deleteResource(Long studyId, Long resourceId) {
        // TODO: 자료 삭제
    }

    private Resource findResource(Long studyId, Long resourceId) {
        return resourceRepository.findByIdAndStudyId(resourceId, studyId).orElseThrow(
                () -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }


    //파일 데이터 DB 및 S3에 반영
    private void fileUploadDbAndS3(List<MultipartFile> files, List<FileDetailDto> list, Resource resource) {
        //파일 데이터 db 저장
        IntStream.range(0, list.size())
                .forEach(index ->fileRepository.save(
                        File.ofResource(resource, list.get(index))
                ));
        //파일 데이터 s3에 업로드
        List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
        s3Uploader.uploadFiles(keys, files);
    }
}