package com.study.focus.announcement.service;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepo;
    private  final StudyMemberRepository studyMemberRepository;
    private  final FileRepository fileRepository;
    private  final S3Uploader s3Uploader;

    //ID를 통해 StudyId에 userId가 포함되는지 확인하여 그룹 내 유효성 검증
    public List<GetAnnouncementsResponse> findAllSummaries(Long studyId, Long userId)
    {
        validation(studyId, userId);
        List<Announcement> resultList = announcementRepo.findAllByStudyId(studyId);
        return  resultList.stream().map(a -> {
            return new GetAnnouncementsResponse(a.getId(), a.getTitle(), a.getCreatedAt());
        }).toList();
    }


    // 공지 생성하기(공지 데이터는 컨트롤러부분에서 유효성 검증을 하기 때문에 검증 x)
    @Transactional
    public Long createAnnouncement(Long studyId, Long userId, String title, String content, List<MultipartFile> files)
    {
        StudyMember userStudyMember = validation(studyId, userId);
        isLeader(userStudyMember);

        Study study = userStudyMember.getStudy();
        Announcement announcement = Announcement.builder().
                study(study).author(userStudyMember).title(title)
                .description(content).build();

        Announcement saveAnnouncements = announcementRepo.save(announcement);
        //파일이 있는 경우
        if(files !=null && !files.isEmpty()){
            List<FileDetailDto> list = files.stream().map(s3Uploader::makeMetaData).toList();
           //파일 데이터 db 저장
           IntStream.range(0,list.size())
                   .forEach(index ->fileRepository.save(
                           File.ofAnnouncement(announcement,list.get(index))
                   ));
           //파일 데이터 s3에 업로드
            List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys,files);
        }
        return saveAnnouncements.getId();
    }

    private static void isLeader(StudyMember userStudyMember) {
        if(!userStudyMember.getRole().equals(StudyRole.LEADER))
        {
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }
    }

    // 공지 상세 데이터 가져오기
    public void getAnnouncementDetail(Long studyId, Long announcementId) {
        // TODO: 공지 상세 조회
    }

    // 공지 수정하기
    public void updateAnnouncement(Long studyId, Long announcementId) {
        // TODO: 공지 수정
    }

    // 공지 삭제하기
    public void deleteAnnouncement(Long studyId, Long announcementId) {
        // TODO: 공지 삭제
    }

    //인터셉터 및 Aop 반영 시 수정 필요
    private StudyMember validation(Long studyId, Long userId) {
        if(studyId == null || userId == null) {throw new BusinessException(CommonErrorCode.INVALID_REQUEST);}
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId).
                orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }


}
