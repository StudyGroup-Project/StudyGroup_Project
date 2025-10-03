package com.study.focus.study.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.SearchStudiesRequest;
import com.study.focus.study.dto.SearchStudiesResponse;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StudyQueryServiceIntegrationTest {

    @Autowired private StudyQueryService studyQueryService;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        // 방장 유저
        User leader1 = User.builder().trustScore(80).build();
        User leader2 = User.builder().trustScore(95).build();

        // 북마크 유저
        User bookmarker = User.builder().trustScore(30).build();

        userRepository.saveAll(List.of(leader1, leader2, bookmarker));

        // 스터디 1
        Study study1 = Study.builder()
                .maxMemberCount(10)
                .build();

        StudyProfile profile1 = StudyProfile.builder()
                .study(study1)
                .title("알고리즘 스터디")
                .bio("백준 같이 풀기")
                .category(Category.IT)
                .address(new Address("경상북도", "경산시"))
                .build();

        StudyMember member1 = StudyMember.builder()
                .study(study1)
                .user(leader1)   // 이제 영속 상태 User 참조
                .role(StudyRole.LEADER)
                .build();

        // 스터디 2
        Study study2 = Study.builder()
                .maxMemberCount(15)
                .build();

        StudyProfile profile2 = StudyProfile.builder()
                .study(study2)
                .title("네트워크 스터디")
                .bio("OSI 7계층")
                .category(Category.IT)
                .address(new Address("경상북도", "경산시"))
                .build();

        StudyMember member2 = StudyMember.builder()
                .study(study2)
                .user(leader2)   // 영속 상태 User 참조
                .role(StudyRole.LEADER)
                .build();

        // 저장
        studyRepository.saveAll(List.of(study1, study2));
        studyProfileRepository.saveAll(List.of(profile1, profile2));
        studyMemberRepository.saveAll(List.of(member1, member2));

        userId = bookmarker.getId();

        // bookmarker가 study1 북마크
        Bookmark bookmark = Bookmark.builder()
                .user(bookmarker)  // 영속 상태 User 참조
                .study(study1)
                .build();
        bookmarkRepository.save(bookmark);
    }

    @Test
    @DisplayName("키워드 검색 동작 확인")
    void searchByKeyword() {
        SearchStudiesRequest req = new SearchStudiesRequest();
        req.setKeyword("알고리즘");

        SearchStudiesResponse res = studyQueryService.searchStudies(req, userId);
        assertThat(res.getStudies()).hasSize(1);
        assertThat(res.getStudies().get(0).getTitle()).contains("알고리즘");
    }

    @Test
    @DisplayName("카테고리 검색 동작 확인")
    void searchByCategory() {
        SearchStudiesRequest req = new SearchStudiesRequest();
        req.setCategory(Category.IT);

        SearchStudiesResponse res = studyQueryService.searchStudies(req, userId);
        assertThat(res.getStudies()).hasSize(2);
    }

    @Test
    @DisplayName("정렬: 방장 trustScore 내림차순")
    void sortByLeaderTrustScore() {
        SearchStudiesRequest req = new SearchStudiesRequest();
        req.setSort(StudySortType.TRUST_SCORE_DESC);

        SearchStudiesResponse res = studyQueryService.searchStudies(req, userId);
        assertThat(res.getStudies().get(0).getTrustScore())
                .isGreaterThanOrEqualTo(res.getStudies().get(1).getTrustScore());
    }

    @Test
    @DisplayName("bookmarked 여부 확인")
    void bookmarkedCheck() {
        SearchStudiesResponse res = studyQueryService.searchStudies(new SearchStudiesRequest(), userId);
        assertThat(res.getStudies().stream().anyMatch(s -> s.isBookmarked())).isTrue();
    }
}
