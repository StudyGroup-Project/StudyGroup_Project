package com.study.focus.notification;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.notification.domain.AudienceType;
import com.study.focus.notification.domain.Notification;
import com.study.focus.notification.repository.NotificationRepository;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private NotificationRepository notificationRepository;

    private User user;
    private Study study;
    private StudyMember member;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // 유저 저장
        user = userRepository.save(User.builder()
                .trustScore(50L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        // 유저 프로필 저장
        userProfileRepository.save(UserProfile.builder()
                .user(user)
                .nickname("홍길동")
                .address(new Address("서울특별시", "강남구"))
                .birthDate(LocalDate.of(2000, 1, 1))  // ✅ 이전에 빠졌던 필드
                .job(Job.STUDENT)                     // ✅ 지금 추가해야 하는 필드
                .preferredCategory(Category.IT)
                .build());



        // 스터디 저장
        study = studyRepository.save(Study.builder()
                .maxMemberCount(10)
                .build());

        // 스터디 멤버 등록
        member = studyMemberRepository.save(StudyMember.builder()
                .study(study)
                .user(user)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());

        // 알림 저장
        notification = notificationRepository.save(Notification.builder()
                .study(study)
                .actor(member)
                .audienceType(AudienceType.ALL_MEMBERS)
                .title("과제 생성 알림")
                .description("1주차 과제가 등록되었습니다.")
                .build());
    }

    @Test
    @DisplayName("성공: 알림 목록 조회 API")
    void getNotifications_success() throws Exception {
        mockMvc.perform(get("/api/studies/{studyId}/notifications", study.getId())
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notification.getId()))
                .andExpect(jsonPath("$[0].title").value("과제 생성 알림"));
    }

    @Test
    @DisplayName("성공: 알림 상세 데이터 조회 API")
    void getNotificationDetail_success() throws Exception {
        mockMvc.perform(get("/api/studies/{studyId}/notifications/{notificationId}", study.getId(), notification.getId())
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("과제 생성 알림"))
                .andExpect(jsonPath("$.description").value("1주차 과제가 등록되었습니다."));
        // createdAt은 비교하지 않음
    }

    @Test
    @DisplayName("실패: 존재하지 않는 알림 ID → 400 반환")
    void getNotificationDetail_fail_notFound() throws Exception {
        mockMvc.perform(get("/api/studies/{studyId}/notifications/{notificationId}", study.getId(), 9999L)
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("잘못된 요청입니다"));
    }

    @Test
    @DisplayName("실패: 스터디 ID가 잘못된 경우 → 400 반환")
    void getNotifications_fail_invalidStudy() throws Exception {
        mockMvc.perform(get("/api/studies/{studyId}/notifications", 9999L)
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("잘못된 요청입니다"));
    }
}
