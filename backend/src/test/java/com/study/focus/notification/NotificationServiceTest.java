package com.study.focus.notification;

import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.service.GroupService;
import com.study.focus.notification.domain.AudienceType;
import com.study.focus.notification.domain.Notification;
import com.study.focus.notification.dto.GetNotificationDetailResponse;
import com.study.focus.notification.dto.GetNotificationsListResponse;
import com.study.focus.notification.repository.NotificationRepository;
import com.study.focus.notification.service.NotificationService;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private StudyRepository studyRepository;
    @Mock private GroupService groupService;

    @InjectMocks private NotificationService notificationService;

    private Study study;
    private StudyMember actor;
    private Notification notification;

    @BeforeEach
    void setUp() {
        study = Study.builder().id(1L).build();
        actor = StudyMember.builder().id(1L).study(study).build();
        notification = Notification.builder()
                .id(10L)
                .study(study)
                .actor(actor)
                .title("테스트 알림")
                .description("테스트 내용")
                .audienceType(AudienceType.ALL_MEMBERS)
                .build();
    }

    @Test
    @DisplayName("알림 목록을 정상적으로 조회한다")
    void getNotifications_success() {
        // given
        given(studyRepository.findById(1L)).willReturn(Optional.of(study));
        given(notificationRepository.findAllByStudy(study)).willReturn(List.of(notification));

        // when
        List<GetNotificationsListResponse> result = notificationService.getNotifications(1L, 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 알림");
        verify(groupService).memberValidation(1L, 1L);
    }

    @Test
    @DisplayName("알림 상세 데이터를 정상적으로 조회한다")
    void getNotificationDetail_success() {
        // given
        given(studyRepository.findById(1L)).willReturn(Optional.of(study));
        given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

        // when
        GetNotificationDetailResponse result = notificationService.getNotificationDetail(1L, 10L, 1L);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 알림");
        assertThat(result.getDescription()).isEqualTo("테스트 내용");
        verify(groupService).memberValidation(1L, 1L);
    }

    @Test
    @DisplayName("스터디가 존재하지 않으면 BusinessException이 발생한다")
    void getNotificationDetail_fail_invalidStudy() {
        // given
        given(studyRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.getNotificationDetail(1L, 10L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("과제 생성 알림을 저장한다")
    void addAssignmentNotice_success() {
        // given
        given(notificationRepository.save(any(Notification.class)))
                .willReturn(notification);

        // when
        notificationService.addAssignmentNotice(study, actor, "1주차 과제");

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
