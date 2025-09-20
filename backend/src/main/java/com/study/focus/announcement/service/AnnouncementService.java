package com.study.focus.announcement.service;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepo;
    private  final StudyMemberRepository studyMemberRepository;

    //ID를 통해 StudyId에 userId가 포함되는지 확인하여 그룹 내 유효성 검증
    public List<GetAnnouncementsResponse> findAllSummaries(Long studyId, Long userId)
    {
        validation(studyId, userId);
        List<Announcement> resultList = announcementRepo.findAllByStudyId(studyId);
        return  resultList.stream().map(a -> {
            return new GetAnnouncementsResponse(a.getId(), a.getTitle(), a.getCreatedAt());
        }).toList();
    }



    // 공지 생성하기
    public void createAnnouncement(Long studyId) {
        // TODO: 공지 생성
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
    private void validation(Long studyId, Long userId) {
        if(studyId == null || userId == null) {throw new BusinessException(CommonErrorCode.INVALID_REQUEST);}
        boolean isStudyMember = studyMemberRepository.existsByStudyIdAndUserId(studyId, userId);
        if(!isStudyMember){

            throw  new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }
    }
}
