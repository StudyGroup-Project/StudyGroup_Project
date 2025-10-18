package com.study.focus.study.service;

import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.notification.service.NotificationService;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final NotificationService notificationService;


    // 멤버 목록 가져오기
    public void getMembers(Long studyId) {
        // TODO: 스터디 멤버 조회
    }

    // 그룹 탈퇴
    @Transactional
    public void leaveStudy(Long studyId, Long requestUserId) {
        // 스터디 멤버 확인
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, requestUserId,StudyMemberStatus.JOINED)
                .orElseThrow(()->new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 방장 탈퇴 불가 (위임 과정이 없기때문에)
        if(studyMember.getRole() == StudyRole.LEADER){
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // 멤버 상태 변경 (soft)
        studyMember.updateStatus(StudyMemberStatus.LEFT);
    }

    // 그룹 인원 추방하기 (방장)
    public void expelMember(Long studyId, Long expelUserId, Long requestUserId) {
        // 방장 권한 확인
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(()->new BusinessException(CommonErrorCode.INVALID_REQUEST));
        if(!leaderMember.getUser().getId().equals(requestUserId)){
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }

        // 자기 추방 불가
        if(requestUserId.equals(expelUserId)){
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // 추방대상이 그룹원인지 확인
        StudyMember memberExpel = studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, expelUserId, StudyMemberStatus.JOINED)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 멤버 추방
        memberExpel.updateStatus(StudyMemberStatus.BANNED);
        // 멤버 추방 알림 생성
        notificationService.addOutMemberNotice(leaderMember.getStudy(),memberExpel.getUser().getId());
    }
}