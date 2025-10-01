package com.study.focus.announcement.service;

import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.domain.Comment;
import com.study.focus.announcement.dto.*;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.common.GroupService;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private  final FileRepository fileRepository;
    private  final S3Uploader s3Uploader;
    private  final CommentRepository commentRepository;
    private final UserService userService;
    private  final GroupService groupService;

    //ID를 통해 StudyId에 userId가 포함되는지 확인하여 그룹 내 유효성 검증
    public List<GetAnnouncementsResponse> findAllSummaries(Long studyId, Long userId)
    {
        groupService.memberValidation(studyId,userId);
        List<Announcement> resultList = announcementRepository.findAllByStudyId(studyId);
        return  resultList.stream().map(a -> {
            return new GetAnnouncementsResponse(a.getId(), a.getTitle(), a.getCreatedAt());
        }).toList();
    }


    // 공지 생성하기(공지 데이터는 컨트롤러부분에서 유효성 검증을 하기 때문에 검증 x)
    @Transactional
    public Long createAnnouncement(Long studyId, Long userId, String title, String content, List<MultipartFile> files)
    {
        StudyMember userStudyMember = groupService.memberValidation(studyId,userId);
        groupService.isLeader(userStudyMember);

        Study study = userStudyMember.getStudy();
        Announcement announcement = Announcement.builder().
                study(study).author(userStudyMember).title(title)
                .description(content).build();

        Announcement saveAnnouncements = announcementRepository.save(announcement);
        //파일이 있는 경우
        if(files !=null && !files.isEmpty()){
            List<FileDetailDto> list = files.stream().map(s3Uploader::makeMetaData).toList();
            fileUploadDbAndS3(files, list, announcement);
        }
        return saveAnnouncements.getId();
    }


    // 공지 삭제하기
    @Transactional
    public void deleteAnnouncement(Long studyId, Long userId, Long announcementId) {
        //검증
        StudyMember userStudyMember = groupService.memberValidation(studyId,userId);
        groupService.isLeader(userStudyMember);
        Announcement findAnnouncement = validationAnnouncement(announcementId, studyId, userStudyMember.getId());


        //파일 삭제
        List<File> findAnnouncementFiles = fileRepository.findAllByAnnouncement_Id(announcementId);
        findAnnouncementFiles.forEach(File::deleteAnnouncementFile);
        //null 반영
        fileRepository.saveAll(findAnnouncementFiles);
        fileRepository.flush();

        //댓글 삭제
        List<Comment> findAnnouncementComments = commentRepository.findAllByAnnouncement_Id(announcementId);
        commentRepository.deleteAll(findAnnouncementComments);

        //공지 삭제
        announcementRepository.delete(findAnnouncement);
    }


    // 공지 수정하기
    @Transactional
    public void updateAnnouncement(Long studyId, Long announcementId, Long userId, AnnouncementUpdateDto updateDto) {
        //검증
        //1. 스터디 멤버 검증
        StudyMember userStudyMember = groupService.memberValidation(studyId,userId);
        //2. 방장 검증
        groupService.isLeader(userStudyMember);
        //3. 공지 검증
        Announcement oldAnnouncement = validationAnnouncement(announcementId, studyId, userStudyMember.getId());
        //4. 내용 업데이트
        oldAnnouncement.updateAnnouncement(updateDto.getTitle(), updateDto.getContent());

        //5. 파일 처리
        //5-1. 삭제할 파일이 있는 경우
        if(updateDto.getDeleteFileIds() !=null&& !updateDto.getDeleteFileIds().isEmpty())
        {
            List<File> deleteFiles = fileRepository.findAllById(updateDto.getDeleteFileIds());
            deleteFiles.forEach(File::deleteAnnouncementFile);
            fileRepository.saveAll(deleteFiles);
            fileRepository.flush();
        }
        //5-2. 새로 추가할 파일이 있는 경우
        if(updateDto.getFiles() != null && !updateDto.getFiles().isEmpty()) {
            List<FileDetailDto> list = updateDto.getFiles().stream().map(s3Uploader::makeMetaData).toList();
            fileUploadDbAndS3(updateDto.getFiles(), list, oldAnnouncement);
        }
    }
    //공지 상세 데이터 가져오기
    public GetAnnouncementDetailResponse getAnnouncementDetail(Long studyId, Long announcementId, Long userId) {
        // 멤버 검증
        groupService.memberValidation(studyId,userId);

        //공지 가져오기
        Announcement announcement = findAnnouncement(studyId, announcementId);
        GetMyProfileResponse authorProfile = userService.getMyProfile(announcement.getAuthor().getUser().getId());

        //공지 댓글 가져오기
        List<Comment> comments = commentRepository.findAllByAnnouncement_Id(announcementId);
        List<AnnouncementComments> announcementComments = makeCommentToAnnouncementComment(comments);

        //공지 파일 가져오기
        List<File> files = fileRepository.findAllByAnnouncement_Id(announcementId);
        List<AnnouncementFiles> announcementFiles = makeFileToAnnouncementFile(files);

        return GetAnnouncementDetailResponse.builder().
                announcementId(announcementId).studyId(studyId).
                title(announcement.getTitle()).content(announcement.getDescription()).
                updatedAt(announcement.getUpdatedAt()).userName(authorProfile.getNickname()).
                userProfileImageUrl(authorProfile.getProfileImageUrl())
                .comments(announcementComments).files(announcementFiles).build();
    }



    // 공지 상세 화면 댓글 작성
    @Transactional
    public void addComment(Long studyId, Long announcementId,Long userId,
                           CreateCommentRequest commentRequest) {
        //1. 스터디 멤버 검증
        StudyMember userStudyMember = groupService.memberValidation(studyId,userId);

        //2.공지 가져오기
        Announcement announcement = findAnnouncement(studyId, announcementId);

        //3. 댓글 저장
        commentRepository.save(
                Comment.builder().announcement(announcement)
                .commenter(userStudyMember)
                .content(commentRequest.getContent())
                .build());
    }


    @NotNull
    private List<AnnouncementComments> makeCommentToAnnouncementComment(List<Comment> comments) {
        return comments.stream().map(c -> {
            GetMyProfileResponse commentUserProfile = userService.getMyProfile(c.getCommenter().getUser().getId());
            return AnnouncementComments.builder()
                    .commentId(c.getId())
                    .userName(commentUserProfile.getNickname())
                    .userProfileImageUrl(commentUserProfile.getProfileImageUrl())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }).toList();
    }

    @NotNull
    private List<AnnouncementFiles> makeFileToAnnouncementFile(List<File> files) {
        return files.stream().map(f -> {
            String fileUrl = s3Uploader.getUrlFile(f.getFileKey());
            return AnnouncementFiles.builder()
                    .fileId(f.getId())
                    .fileName(f.getFileName())
                    .fileUrl(fileUrl)
                    .build();
        }).toList();
    }

    private  Announcement findAnnouncement(Long studyId, Long announcementId) {
        return announcementRepository.findByIdAndStudyId(announcementId, studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }

    private void fileUploadDbAndS3(List<MultipartFile> files, List<FileDetailDto> list, Announcement announcement) {
        //파일 데이터 db 저장
        IntStream.range(0, list.size())
                .forEach(index ->fileRepository.save(
                        File.ofAnnouncement(announcement, list.get(index))
                ));
        //파일 데이터 s3에 업로드
        List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
        s3Uploader.uploadFiles(keys, files);
    }


    private  Announcement validationAnnouncement(Long announcementId, Long studyId, Long authorId)
    {
        if(studyId == null || authorId ==null || announcementId ==null)
        {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }
        Optional<Announcement> announcement = announcementRepository.findByIdAndStudy_IdAndAuthor_Id(announcementId, studyId,authorId);
        return announcement.orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }

}
