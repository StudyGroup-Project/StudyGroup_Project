package com.study.focus.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.study.domain.RecruitStatus;
import com.study.focus.study.domain.Study;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ApplicationRepository applicationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
    }

    @AfterEach
    void tearDown() {
        applicationRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("스터디 지원서 제출 - 성공")
    void submitApplication_Success() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("열심히 하겠습니다!");
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertThat(applicationRepository.count()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("스터디 지원서 제출 실패 - 존재하지 않는 사용자")
    void submitApplication_Fail_UserNotFound() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");
        long nonExistentUserId = 999L;
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(nonExistentUserId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(applicationRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("스터디 지원서 제출 실패 - 모집중이 아님")
    void submitApplication_Fail_NotRecruiting() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.CLOSED).build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(applicationRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("스터디 지원서 제출 실패 - 이미 SUBMITTED 상태로 지원함")
    void submitApplication_Fail_AlreadySubmitted() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).build());
        applicationRepository.save(Application.builder()
                .applicant(testUser)
                .study(testStudy)
                .content("이전 지원서")
                .status(ApplicationStatus.SUBMITTED)
                .build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("중복 지원");
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(applicationRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("스터디 지원서 제출 성공 - 이전 지원서가 REJECTED면 재지원 가능")
    void submitApplication_Success_ResubmitAfterRejected() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).build());
        applicationRepository.save(Application.builder()
                .applicant(testUser)
                .study(testStudy)
                .content("이전 지원서")
                .status(ApplicationStatus.REJECTED)
                .build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("재지원합니다");
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertThat(applicationRepository.count()).isEqualTo(initialCount + 1);
    }

}
