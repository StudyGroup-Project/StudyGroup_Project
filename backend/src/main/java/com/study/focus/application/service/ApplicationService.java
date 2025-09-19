package com.study.focus.application.service;

import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    // 지원서 제출하기
    public void submitApplication(Long studyId) {
        // TODO: 지원서 생성
    }

    // 지원서 목록 가져오기
    public void getApplications(Long studyId) {
        // TODO: 지원서 목록 조회
    }

    // 지원서 상세 가져오기
    public void getApplicationDetail(Long studyId, Long applicationId) {
        // TODO: 지원서 상세 조회
    }

    // 지원서 처리하기 (승인/거절 등)
    public void handleApplication(Long studyId, Long applicationId) {
        // TODO: 지원서 상태 변경
    }
}
