package com.study.focus.study.service;

import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;


    // 멤버 목록 가져오기
    public void getMembers(Long studyId) {
        // TODO: 스터디 멤버 조회
    }

    // 그룹 탈퇴
    public void leaveStudy(Long studyId, Long userId) {
        // TODO: 멤버 탈퇴 처리
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
        StudyMember memberExpel = studyMemberRepository.findByStudyIdAndUserId(studyId, expelUserId)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 멤버 추방
        studyMemberRepository.delete(memberExpel);

    }
}