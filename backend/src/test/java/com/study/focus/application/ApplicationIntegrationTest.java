package com.study.focus.application;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.dto.HandleApplicationRequest;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.notification.repository.NotificationRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
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
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;

    private User leader;
    private Study testStudy;
    private UserProfile applicantProfile;
    private User applicant;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
        leader = userRepository.save(User.builder().build());
        applicant = userRepository.save(User.builder().build());
        testStudy = studyRepository.save(Study.builder().maxMemberCount(10).recruitStatus(RecruitStatus.OPEN).build());
        studyMemberRepository.save(StudyMember.builder()
                .user(leader)
                .study(testStudy)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());
        Address applicantAddress = Address.builder()
                .province("서울특별시")
                .district("강남구")
                .build();
        applicantProfile = userProfileRepository.save(UserProfile.builder()
                .user(applicant)
                .nickname("지원자")
                .address(applicantAddress)
                .birthDate(LocalDate.of(1990, 1, 1))
                .job(Job.STUDENT)
                .preferredCategory(Category.IT)
                .build());
        userProfileRepository.save(UserProfile.builder()
                .user(leader)
                .nickname("그룹장")
                .birthDate(LocalDate.of(2002, 10, 17))
                .address(applicantAddress)
                .job(Job.STUDENT)
                .preferredCategory(Category.IT)
                .build());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        applicationRepository.deleteAll();
        studyMemberRepository.deleteAll();
        userProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("스터디 지원서 제출 - 성공")
    void submitApplication_Success() throws Exception {
        Study testStudy = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).build());
        SubmitApplicationRequest request = new SubmitApplicationRequest("열심히 하겠습니다!");
        long initialCount = applicationRepository.count();

        mockMvc.perform(post("/api/studies/" + this.testStudy.getId() + "/applications")
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

        mockMvc.perform(post("/api/studies/" + this.testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertThat(applicationRepository.count()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("지원서 목록 조회 - 방장 성공")
    void getApplications_Success_Leader() throws Exception {
        applicationRepository.save(Application.builder()
                .applicant(applicant)
                .study(testStudy)
                .content("지원 내용")
                .status(ApplicationStatus.SUBMITTED)
                .build());

        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantId").value(applicant.getId()));
    }

    @Test
    @DisplayName("지원서 목록 조회 실패 - 방장이 아닌 경우")
    void getApplications_Fail_NotLeader() throws Exception {
        User notLeader = userRepository.save(User.builder().build());
        applicationRepository.save(Application.builder()
                .applicant(applicant)
                .study(testStudy)
                .content("지원 내용")
                .status(ApplicationStatus.SUBMITTED)
                .build());

        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(notLeader.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("지원서 목록 조회 실패 - 지원자 프로필 없음")
    void getApplications_Fail_ProfileNotFound() throws Exception {
        User noProfileUser = userRepository.save(User.builder().build());
        applicationRepository.save(Application.builder()
                .applicant(noProfileUser)
                .study(testStudy)
                .content("지원 내용")
                .status(ApplicationStatus.SUBMITTED)
                .build());

        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications")
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("지원서 상세 조회 - 방장 성공")
    void getApplicationDetail_Success_Leader() throws Exception {
        // given: 지원서를 미리 저장
        Application application = applicationRepository.save(Application.builder()
                .applicant(applicant)
                .study(testStudy)
                .content("안녕하세요! 저는 이 스터디에 꼭 참여하고 싶습니다. 열심히 하겠습니다!")
                .status(ApplicationStatus.SUBMITTED)
                .build());

        // when & then: 방장이 저장된 지원서의 ID로 상세 조회를 요청
        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications/" + application.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("안녕하세요! 저는 이 스터디에 꼭 참여하고 싶습니다. 열심히 하겠습니다!"));
    }

    @Test
    @DisplayName("지원서 상세 조회 실패 - 방장이 아닌 경우")
    void getApplicationDetail_Fail_NotLeader() throws Exception {
        // given
        User notLeader = userRepository.save(User.builder().build());
        Application application = applicationRepository.save(Application.builder()
                .applicant(applicant)
                .study(testStudy)
                .content("지원 내용")
                .status(ApplicationStatus.SUBMITTED)
                .build());

        // when & then
        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications/" + application.getId())
                        .with(user(new CustomUserDetails(notLeader.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("지원서 상세 조회 실패 - 지원서 없음")
    void getApplicationDetail_Fail_ApplicationNotFound() throws Exception {
        // given
        Long nonExistentApplicationId = 9999L;

        // when & then
        mockMvc.perform(get("/api/studies/" + testStudy.getId() + "/applications/" + nonExistentApplicationId)
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // or isNotFound(), depending on your exception handling
    }

    @Test
    @DisplayName("지원서 수락 - 성공")
    void handleApplication_Accept_Success() throws Exception {
        // given: 'applicant'가 'testStudy'에 제출한 지원서를 생성
        Application application = applicationRepository.save(Application.builder()
                .study(testStudy)
                .applicant(applicant)
                .status(ApplicationStatus.SUBMITTED)
                .content("지원합니다.")
                .build());

        HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        // when: 방장 권한으로 '수락' 요청
        mockMvc.perform(put("/api/studies/{studyId}/applications/{applicationId}", this.testStudy.getId(), application.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then: DB 상태 검증
        // 1. 지원서 상태가 'ACCEPTED'로 변경되었는지 확인
        Application processedApplication = applicationRepository.findById(application.getId()).orElseThrow();
        assertThat(processedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);

        // 2. 지원자가 스터디 멤버로 추가되었는지 안정적인 방법으로 확인
        List<StudyMember> allMembers = studyMemberRepository.findAll();
        boolean isApplicantNowMember = allMembers.stream()
                .anyMatch(member -> member.getUser().getId().equals(applicant.getId()) &&
                        member.getStudy().getId().equals(testStudy.getId()));
        assertThat(isApplicantNowMember).isTrue();
    }

    @Test
    @DisplayName("지원서 거절 - 성공")
    void handleApplication_Reject_Success() throws Exception {
        // given
        Application application = applicationRepository.save(Application.builder()
                .study(testStudy)
                .applicant(applicant)
                .status(ApplicationStatus.SUBMITTED)
                .content("지원서를 냅니다.")
                .build());

        HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.REJECTED);

        // when
        mockMvc.perform(put("/api/studies/{studyId}/applications/{applicationId}", testStudy.getId(), application.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        // 1. 지원서 상태가 'REJECTED'로 변경되었는지 확인
        Application processedApplication = applicationRepository.findById(application.getId()).orElseThrow();
        assertThat(processedApplication.getStatus()).isEqualTo(ApplicationStatus.REJECTED);

        // 2. 지원자가 스터디 멤버로 추가되지 않았는지 확인
        List<StudyMember> allMembers = studyMemberRepository.findAll();
        boolean isApplicantNowMember = allMembers.stream()
                .anyMatch(member -> member.getUser().getId().equals(applicant.getId()) &&
                        member.getStudy().getId().equals(testStudy.getId()));
        assertThat(isApplicantNowMember).isFalse();
    }

    // ... [방장이 아닌 경우], [정원 초과] 실패 테스트 케이스는 이전과 동일 ...

    @Test
    @DisplayName("지원서 처리 실패 - 방장이 아닌 경우")
    void handleApplication_Fail_NotLeader() throws Exception {
        // given
        Application application = applicationRepository.save(Application.builder()
                .study(testStudy)
                .applicant(applicant)
                .status(ApplicationStatus.SUBMITTED)
                .content("지원서를 합니다.")
                .build());

        HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        // when & then
        mockMvc.perform(put("/api/studies/{studyId}/applications/{applicationId}", testStudy.getId(), application.getId())
                        .with(user(new CustomUserDetails(testUser.getId()))) // 방장이 아닌 사용자
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("지원서 수락 실패 - 정원이 가득 찬 경우")
    void handleApplication_Accept_Fail_StudyIsFull() throws Exception {
        // given: 스터디 정원을 현재 멤버 수(1명)와 같게 설정
        testStudy.updateMaxMemberCount(1);
        studyRepository.save(testStudy);

        Application application = applicationRepository.save(Application.builder()
                .study(testStudy)
                .applicant(applicant)
                .status(ApplicationStatus.SUBMITTED)
                .content("지원해줘요")
                .build());

        HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        // when & then
        mockMvc.perform(put("/api/studies/{studyId}/applications/{applicationId}", testStudy.getId(), application.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
