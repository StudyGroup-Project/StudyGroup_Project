package com.study.focus.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class StudyQueryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private TokenProvider tokenProvider;

    private final ObjectMapper om = new ObjectMapper();

    /**
     * 로그인(JSON) → accessToken 추출 헬퍼 메서드
     */
    private String loginAndGetAccessToken(String loginId, String password) throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "loginId": "%s",
                              "password": "%s"
                            }
                            """.formatted(loginId, password)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> result = om.readValue(responseBody, Map.class);

        return (String) result.get("accessToken");
    }

    @Test
    @DisplayName("회원가입 후 로그인 + 스터디 생성 → 검색 API 호출 시 200 OK와 결과 반환")
    void searchStudies_returnsJsonResponse_withRegisterAndLogin() throws Exception {

        // 1. 회원가입
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "loginId": "testuser",
                              "password": "1234",
                              "nickname": "tester"
                            }
                            """))
                .andExpect(status().isOk());

        // 2. 로그인(JSON)
        String accessToken = loginAndGetAccessToken("testuser", "1234");

        // 3. User 식별
        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        User leader = userRepository.findById(userId).orElseThrow();

        // 4. 테스트용 스터디 생성
        Study study = Study.builder()
                .maxMemberCount(10)
                .build();
        studyRepository.save(study);

        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("알고리즘")
                .bio("백준 같이 풀기")
                .category(List.of(Category.IT))
                .address(new Address("경상북도", "경산시"))
                .build();
        studyProfileRepository.save(profile);

        StudyMember member = StudyMember.builder()
                .study(study)
                .user(leader)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();
        studyMemberRepository.save(member);

        // 5. 검색 API 호출
        mockMvc.perform(get("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "알고리즘")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("sort", "LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studies[0].title").value("알고리즘"));
    }

    @Test
    @DisplayName("내 그룹 목록 보기: 가입(JOINED)된 스터디만 반환")
    void myStudies_returnsJsonResponse_withRegisterAndLogin() throws Exception {

        // 1) 회원가입
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "loginId": "mygroup_user",
                          "password": "1234",
                          "nickname": "mygroup"
                        }
                        """))
                .andExpect(status().isOk());

        // 2) 로그인(JSON)
        String accessToken = loginAndGetAccessToken("mygroup_user", "1234");

        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        User me = userRepository.findById(userId).orElseThrow();

        // 3) 테스트 데이터
        Study study = Study.builder()
                .maxMemberCount(20)
                .build();
        studyRepository.save(study);

        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("내가 가입한 스터디")
                .bio("알고리즘 스터디")
                .category(List.of(Category.IT))
                .address(new Address("경상북도", "경산시"))
                .build();
        studyProfileRepository.save(profile);

        StudyMember member = StudyMember.builder()
                .study(study)
                .user(me)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();
        studyMemberRepository.save(member);

        // 4) 호출 & 검증
        mockMvc.perform(get("/api/studies/mine")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studies[0].title").value("내가 가입한 스터디"));
    }

    @Test
    @DisplayName("내 찜 목록 보기: 북마크한 스터디만 반환")
    void myBookmarkedStudies_returnsJsonResponse_withRegisterAndLogin() throws Exception {

        // 1) 회원가입
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "loginId": "bookmark_user",
                          "password": "1234",
                          "nickname": "bookmarker"
                        }
                        """))
                .andExpect(status().isOk());

        // 2) 로그인(JSON)
        String accessToken = loginAndGetAccessToken("bookmark_user", "1234");

        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        User me = userRepository.findById(userId).orElseThrow();

        // 3) 테스트용 스터디/프로필 생성
        Study study = Study.builder()
                .maxMemberCount(15)
                .build();
        studyRepository.save(study);

        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("내가 찜한 스터디")
                .bio("알고리즘 스터디")
                .category(List.of(Category.IT))
                .address(new Address("경상북도", "경산시"))
                .build();
        studyProfileRepository.save(profile);

        StudyMember member = StudyMember.builder()
                .study(study)
                .user(me)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();
        studyMemberRepository.save(member);

        // 4) 북마크 생성
        Bookmark bm = Bookmark.builder()
                .study(study)
                .user(me)
                .build();
        bookmarkRepository.save(bm);

        // 5) 호출 & 검증
        mockMvc.perform(get("/api/studies/bookmarks")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studies[0].title").value("내가 찜한 스터디"));
    }
}
