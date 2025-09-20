package com.study.focus.study.service;

import org.springframework.stereotype.Service;

@Service
public class StudyMemberService {

    // 멤버 목록 가져오기
    public void getMembers(Long studyId) {
        // TODO: 스터디 멤버 조회
    }

    // 그룹 탈퇴
    public void leaveStudy(Long studyId, Long userId) {
        // TODO: 멤버 탈퇴 처리
    }

    // 그룹 인원 추방하기 (방장)
    public void expelMember(Long studyId, Long userId) {
        // TODO: 멤버 추방 처리
    }
}