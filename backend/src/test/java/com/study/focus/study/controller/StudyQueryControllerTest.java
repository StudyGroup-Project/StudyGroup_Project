package com.study.focus.study.controller;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyProfile;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudyQueryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private TokenProvider tokenProvider;

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

        // 2. 로그인 → Redirect URL 추출
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Form 데이터 타입
                        .param("loginId", "testuser")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectUrl = loginResult.getResponse().getRedirectedUrl();

        // 3. Redirect URL에서 accessToken 파싱
        URI uri = new URI(redirectUrl);
        String query = uri.getQuery(); // e.g. token=xxxx&profileExists=false
        Map<String, String> params = Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        String accessToken = params.get("accessToken");

        // 4. 테스트용 Study/StudyProfile/StudyMember 데이터 삽입
        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        User leader = userRepository.findById(userId).orElseThrow();


        Study study = Study.builder()
                .maxMemberCount(10)
                .build();
        studyRepository.save(study);

        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("알고리즘")
                .bio("백준 같이 풀기")
                .category(Category.IT)
                .address(new Address("경상북도", "경산시")) 
                .build();
        studyProfileRepository.save(profile);

        StudyMember member = StudyMember.builder()
                .study(study)
                .user(leader)
                .role(StudyRole.LEADER)
                .build();
        studyMemberRepository.save(member);

        // 5. 토큰 넣고 /api/studies 요청
        mockMvc.perform(get("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "알고리즘")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("sort", "LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studies[0].title").value("알고리즘"));
    }
}
