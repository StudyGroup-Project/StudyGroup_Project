package com.study.focus.study.service;

import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.dto.StudyDto;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.StudyProfile;
import com.study.focus.study.domain.StudySortType;
import com.study.focus.study.dto.SearchStudiesRequest;
import com.study.focus.study.dto.SearchStudiesResponse;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyQueryService {

    private final StudyRepository studyRepository;
    private final StudyProfileRepository studyProfileRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final S3Uploader s3Uploader;
    private final BookmarkRepository bookmarkRepository;

    // 스터디 그룹 검색 요청
    @Transactional(readOnly = true)
    public SearchStudiesResponse searchStudies(SearchStudiesRequest request, Long userId) {
        Pageable pageable = PageRequest.of(
                request.getPageOrDefault(),
                request.getLimitOrDefault(),
                getSort(request.getSort())
        );

        Page<StudyDto> pageResult = studyRepository.searchStudies(
                request.getKeyword(),
                request.getCategory(),
                request.getProvince(),
                request.getDistrict(),
                userId,
                request.getSort(),
                pageable
        );

        SearchStudiesResponse.Meta meta = SearchStudiesResponse.Meta.builder()
                .page(request.getPage())
                .limit(request.getLimitOrDefault())
                .totalCount(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .sort(request.getSort())
                .build();

        return SearchStudiesResponse.builder()
                .studies(pageResult.getContent())
                .meta(meta)
                .build();
    }

    private Sort getSort(StudySortType sortType) {
        return switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case TRUST_SCORE_DESC -> Sort.by(Sort.Direction.DESC, "trustScore");
        };
    }

    // 내 그룹 데이터 가져오기
    public void getMyStudies(Long userId) {
        // TODO: 내가 가입한 스터디 조회
    }

    // 내 찜 목록 가져오기
    public void getBookmarks(Long userId) {
        // TODO: 내가 찜한 스터디 목록 조회
    }
}
