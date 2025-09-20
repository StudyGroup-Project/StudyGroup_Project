package com.study.focus.study.service;

import org.springframework.stereotype.Service;

@Service
public class BookmarkService {

    // 내 찜 목록 가져오기
    public void getBookmarks(Long userId) {
        // TODO: 내가 찜한 스터디 목록 조회
    }

    // 스터디 그룹 찜하기
    public void addBookmark(Long studyId, Long userId) {
        // TODO: 스터디 찜 추가
    }

    // 스터디 그룹 찜 해제하기
    public void removeBookmark(Long studyId, Long userId) {
        // TODO: 스터디 찜 해제
    }
}