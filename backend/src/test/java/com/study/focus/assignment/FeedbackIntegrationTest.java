package com.study.focus.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Feedback;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.EvaluateSubmissionRequest;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.FeedbackRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedbackIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;

    @Autowired StudyRepository studyRepository;
    @Autowired StudyMemberRepository studyMemberRepository;

    @Autowired AssignmentRepository assignmentRepository;
    @Autowired SubmissionRepository submissionRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired FileRepository fileRepository;

    private Study study1;
    private Study study2;
    private User reviewerUser;     // 목록/작성 요청자
    private User submitterUser;    // 과제 제출자
    private StudyMember reviewerMember;
    private StudyMember submitterMember;
    private Assignment assignment1;
    private Submission submission1;

    @BeforeEach
    void setUp() {
        // 유저 & 프로필
        reviewerUser  = userRepository.save(User.builder().trustScore(10L).lastLoginAt(LocalDateTime.now()).build());
        submitterUser = userRepository.save(User.builder().trustScore(20L).lastLoginAt(LocalDateTime.now()).build());

        File img = fileRepository.save(
                File.ofProfileImage(new FileDetailDto("avatar.png", "reviewer-key", "png", 1234L))
        );

        userProfileRepository.save(UserProfile.builder()
                .user(reviewerUser).nickname("reviewerNick")
                .profileImage(img)
                .address(Address.builder().province("p").district("d").build())
                .birthDate(LocalDateTime.now().toLocalDate())
                .job(Job.STUDENT).preferredCategory(List.of(Category.IT)).build());

        // 스터디 2개
        study1 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());
        study2 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());

        // 멤버
        reviewerMember = studyMemberRepository.save(StudyMember.builder()
                .user(reviewerUser).study(study1).role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1)).build());
        submitterMember = studyMemberRepository.save(StudyMember.builder()
                .user(submitterUser).study(study1).role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1)).build());

        // 과제/제출물
        assignment1 = assignmentRepository.save(Assignment.builder()
                .study(study1).title("A1").description("desc")
                .startAt(LocalDateTime.now().minusDays(1))
                .dueAt(LocalDateTime.now().plusDays(1))
                .creator(reviewerMember)
                .build());

        submission1 = submissionRepository.save(Submission.builder()
                .assignment(assignment1).submitter(submitterMember)
                .description("sub1").build());


        UserProfile reviewerProfile = userProfileRepository.findByUserId(reviewerUser.getId()).orElseThrow();

        userProfileRepository.save(reviewerProfile);
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    /* 과자 평가하기 test */

    @Test
    @DisplayName("성공: 평가 작성 → 201 Created + Location")
    void addFeedback_success() throws Exception {
        long before = feedbackRepository.count();

        EvaluateSubmissionRequest req = EvaluateSubmissionRequest.builder()
                .score(4L).content("nice").build();

        mockMvc.perform(post(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/studies/" + study1.getId()
                                + "/assignments/" + assignment1.getId()
                                + "/submissions/" + submission1.getId()
                                + "/feedbacks/")));

        assertThat(feedbackRepository.count()).isEqualTo(before + 1);

        // 신뢰점수 갱신 확인 (submitterUser가 +4 됐는지)
        assertThat(userRepository.findById(submitterUser.getId()).orElseThrow().getTrustScore())
                .isEqualTo(20L + 4L);
    }

    @Test
    @DisplayName("실패: 제출자가 자기 제출물에 평가 시도 → 400")
    void addFeedback_fail_selfReview() throws Exception {
        EvaluateSubmissionRequest req = EvaluateSubmissionRequest.builder()
                .score(2L).content("self").build();

        mockMvc.perform(post(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(submitterUser.getId()))) // 제출자 본인
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 스터디 멤버 아님 → 400")
    void addFeedback_fail_notMember() throws Exception {
        User outsider = userRepository.save(User.builder().trustScore(0L).lastLoginAt(LocalDateTime.now()).build());
        EvaluateSubmissionRequest req = EvaluateSubmissionRequest.builder()
                .score(5L).content("x").build();

        mockMvc.perform(post(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(outsider.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 중복 제출(같은 리뷰어가 같은 제출물 재평가) → 400")
    void addFeedback_fail_duplicate() throws Exception {
        feedbackRepository.save(Feedback.builder()
                .submission(submission1).reviewer(reviewerMember).score(5L).content("once").build());

        EvaluateSubmissionRequest req = EvaluateSubmissionRequest.builder().score(1L).content("dup").build();

        mockMvc.perform(post(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    /* 피드백 조회 test */

    @Test
    @DisplayName("성공: 피드백 목록 조회 - 아이템 존재")
    void getFeedbacks_success_withItems() throws Exception {
        // given: 동일 리뷰어가 제출물에 2개 평가
        feedbackRepository.save(Feedback.builder()
                .submission(submission1).reviewer(reviewerMember).score(5L).content("great").build());
        feedbackRepository.save(Feedback.builder()
                .submission(submission1).reviewer(reviewerMember).score(3L).content("good").build());

        // when & then
        mockMvc.perform(get(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].evaluaterName").value("reviewerNick"))
                .andExpect(jsonPath("$[0].evaluatorProfileUrl").exists());
    }

    @Test
    @DisplayName("성공: 피드백 목록 조회 - 빈 리스트")
    void getFeedbacks_success_empty() throws Exception {
        mockMvc.perform(get(url(study1.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("실패: 스터디 멤버가 아님 → 400")
    void getFeedbacks_fail_notMember() throws Exception {
        // reviewerUser는 study2에 속하지 않음
        mockMvc.perform(get(url(study2.getId(), assignment1.getId(), submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: assignmentId 불일치 → 400")
    void getFeedbacks_fail_invalidAssignment() throws Exception {
        Long wrongAssignmentId = assignmentRepository.save(Assignment.builder()
                .study(study2).title("X").startAt(LocalDateTime.now().minusDays(1))
                .dueAt(LocalDateTime.now().plusDays(1)).creator(reviewerMember).build()).getId();

        mockMvc.perform(get(url(study1.getId(), wrongAssignmentId, submission1.getId()))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: submissionId 불일치 → 400")
    void getFeedbacks_fail_invalidSubmission() throws Exception {
        Long wrongSubmissionId = submissionRepository.save(Submission.builder()
                .assignment(assignment1).submitter(submitterMember).description("other").build()).getId();

        // assignment는 맞지만 submissionId를 존재하지 않는 값으로 (또는 다른 과제의 제출물로)
        mockMvc.perform(get(url(study1.getId(), assignment1.getId(), 999999L))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isBadRequest());

        // 또는 과제/제출물 매칭 불일치
        mockMvc.perform(get(url(study1.getId(), assignment1.getId(), wrongSubmissionId + 1))
                        .with(user(new CustomUserDetails(reviewerUser.getId()))))
                .andExpect(status().isBadRequest());
    }


    private String url(Long studyId, Long assignmentId, Long submissionId) {
        return "/api/studies/" + studyId + "/assignments/" + assignmentId + "/submissions/" + submissionId + "/feedbacks";
    }
}
