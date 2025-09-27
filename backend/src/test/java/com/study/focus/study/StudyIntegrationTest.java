package com.study.focus.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Category;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyProfileRepository studyProfileRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
    }

    @AfterEach
    void tearDown() {
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("스터디 그룹 생성 - 성공")
    void createStudy_Success() throws Exception {
        CreateStudyRequest request = new CreateStudyRequest(
                "통합 테스트 스터디", 10, Category.IT, "서울", "마포구", "API 통합 테스트", "상세 설명"
        );

        long initialStudyCount = studyRepository.count();

        mockMvc.perform(post("/api/studies")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertThat(studyRepository.count()).isEqualTo(initialStudyCount + 1);
    }

    @Test
    @DisplayName("스터디 그룹 생성 실패 - 존재하지 않는 사용자의 요청")
    void createStudy_Fail_UserNotFound() throws Exception {

        CreateStudyRequest request = new CreateStudyRequest(
                "실패할 스터디", 10, Category.IT, "경기", "성남시", "소개", "설명"
        );
        long nonExistentUserId = 999L;

        long initialStudyCount = studyRepository.count();


        mockMvc.perform(post("/api/studies")
                        .with(user(new CustomUserDetails(nonExistentUserId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(studyRepository.count()).isEqualTo(initialStudyCount);
    }
}
