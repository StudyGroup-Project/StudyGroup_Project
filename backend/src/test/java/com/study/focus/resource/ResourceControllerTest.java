package com.study.focus.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Study study;
    private StudyMember studyMember;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .trustScore(50L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        UserProfile userProfile = userProfileRepository.save(UserProfile.builder()
                .user(user)
                .nickname("tester")
                .job(Job.FREELANCER)
                .preferredCategory(Category.IT)
                .address(Address.builder().province("p").district("dis").build())
                .birthDate(LocalDate.now())
                .build());

        study = studyRepository.save(Study.builder()
                .maxMemberCount(20)
                .recruitStatus(RecruitStatus.OPEN)
                .build());

        studyMember = studyMemberRepository.save(StudyMember.builder()
                .user(user)
                .study(study)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());

        resourceRepository.save(Resource.builder()
                .study(study)
                .author(studyMember)
                .title("자료1")
                .description("내용1")
                .build());
    }

    @AfterEach
    void after() {
        resourceRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 자료 목록 조회")
    void getResources_success() throws Exception {
        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources")
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패: 스터디 멤버가 아닌 경우")
    void getResources_fail_notStudyMember() throws Exception {
        User anotherUser = userRepository.save(User.builder()
                .trustScore(10L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources")
                        .with(user(new CustomUserDetails(anotherUser.getId()))))
                .andExpect(status().isBadRequest());
    }


}
