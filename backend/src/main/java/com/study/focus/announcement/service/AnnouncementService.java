package com.study.focus.announcement.service;

import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    // 공지 목록 가져오기
    public void getAnnouncements(Long studyId) {
        // TODO: 스터디별 공지 목록 조회
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
}
