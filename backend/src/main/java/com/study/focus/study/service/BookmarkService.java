package com.study.focus.study.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.ErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.study.domain.Bookmark;
import com.study.focus.study.domain.Study;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // 데이터 변경이 일어나므로 트랜잭션 처리
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;

    // 내 찜 목록 가져오기
    public void getBookmarks(Long userId) {
        // TODO: 내가 찜한 스터디 목록 조회
    }

    // 스터디 그룹 찜하기
    public void addBookmark(Long userId, Long studyId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 이미 찜한 스터디인지 확인 (중복 방지)
        bookmarkRepository.findByUserAndStudy(user, study).ifPresent(bookmark -> {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        });

        // 4. 북마크 객체 생성
        Bookmark newBookmark = Bookmark.builder()
                .user(user)
                .study(study)
                .build();

        bookmarkRepository.save(newBookmark);
    }


    // 스터디 그룹 찜 해제하기
    public void removeBookmark(Long studyId, Long userId) {
        // TODO: 스터디 찜 해제
    }
}