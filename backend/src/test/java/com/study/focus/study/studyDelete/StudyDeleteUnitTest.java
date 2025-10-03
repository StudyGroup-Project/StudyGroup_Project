package com.study.focus.study.studyDelete;



import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.FeedbackRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.chat.repository.ChatMessageRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.FileService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.notification.repository.NotificationRepository;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.dto.GetStudyProfileResponse;
import com.study.focus.study.dto.StudyHomeResponse;
import com.study.focus.study.dto.UpdateStudyProfileRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.study.service.StudyService;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.service.BookmarkService;

@ExtendWith(MockitoExtension.class)
public class StudyDeleteUnitTest {

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyProfileRepository studyProfileRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private S3Uploader s3Uploader;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private StudyService studyService;


    @Test
    @DisplayName("스터디 그룹 삭제 - 성공(방장)")
    void deleteStudy_Success() {
        // given
        Long studyId = 1L;
        Long leaderUserId = 10L;
        StudyMember leader = StudyMember.builder()
                .user(User.builder().id(leaderUserId).build())
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.of(leader));

        given(announcementRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(assignmentRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(resourceRepository.findAllByStudyId(studyId)).willReturn(List.of());

        // when
        studyService.deleteStudy(studyId, leaderUserId);

        // then
        then(studyMemberRepository).should().findByStudyIdAndRoleAndStatus(studyId, StudyRole.LEADER, StudyMemberStatus.JOINED);
        then(announcementRepository).should().findAllByStudyId(studyId);
        then(assignmentRepository).should().findAllByStudyId(studyId);
        then(resourceRepository).should().findAllByStudyId(studyId);
        then(commentRepository).should().deleteAllByAnnouncement_Study_Id(studyId);
        then(feedbackRepository).should().deleteAllBySubmission_Assignment_Study_Id(studyId);
        then(submissionRepository).should().deleteAllByAssignment_Study_Id(studyId);
        then(announcementRepository).should().deleteAllByStudy_Id(studyId);
        then(assignmentRepository).should().deleteAllByStudy_Id(studyId);
        then(applicationRepository).should().deleteAllByStudy_Id(studyId);
        then(bookmarkRepository).should().deleteAllByStudy_Id(studyId);
        then(chatMessageRepository).should().deleteAllByStudy_Id(studyId);
        then(notificationRepository).should().deleteAllByStudy_Id(studyId);
        then(studyMemberRepository).should().deleteAllByStudy_Id(studyId);
        then(studyProfileRepository).should().deleteByStudy_Id(studyId);
        then(resourceRepository).should().deleteAllByStudy_Id(studyId);
        then(studyRepository).should().deleteById(studyId);
    }

    @Test
    @DisplayName("스터디 그룹 삭제 실패 - 방장 아님")
    void deleteStudy_Fail_NotLeader() {
        // given
        Long studyId = 1L;
        Long leaderUserId = 10L;
        Long otherUserId = 20L;
        StudyMember leader = StudyMember.builder()
                .user(User.builder().id(leaderUserId).build())
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.of(leader));

        // when & then
        assertThatThrownBy(() -> studyService.deleteStudy(studyId, otherUserId))
                .isInstanceOf(BusinessException.class);

        then(studyRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("스터디 그룹 삭제 실패 - 방장 정보 없음")
    void deleteStudy_Fail_NoLeader() {
        // given
        Long studyId = 1L;
        Long userId = 10L;

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyService.deleteStudy(studyId, userId))
                .isInstanceOf(BusinessException.class);

        then(studyRepository).should(never()).deleteById(any());
    }


    @Test
    @DisplayName("빈 스터디(자식 엔티티가 없는 경우) 삭제 - 성공")
    void deleteStudy_Success_EmptyStudy() {
        // given
        Long studyId = 1L;
        Long leaderUserId = 10L;
        StudyMember leader = StudyMember.builder()
                .user(User.builder().id(leaderUserId).build())
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.of(leader));
        given(announcementRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(assignmentRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(resourceRepository.findAllByStudyId(studyId)).willReturn(List.of());

        // when
        studyService.deleteStudy(studyId, leaderUserId);

        // then
        then(fileService).should(never()).deleteFilesByAnnouncementId(any());
        then(fileService).should(never()).deleteFilesByAssignmentId(any());
        then(fileService).should(never()).deleteFilesBySubmissionIds(any());
        then(fileService).should(never()).deleteFilesByResourceId(any());
        then(studyRepository).should().deleteById(studyId);
    }

    @Test
    @DisplayName("스터디에 속한 공지/과제/자료 파일 삭제 서비스 호출 검증")
    void deleteStudy_FileServiceInvocation() {
        // given
        Long studyId = 1L;
        Long leaderUserId = 10L;
        StudyMember leader = StudyMember.builder()
                .user(User.builder().id(leaderUserId).build())
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();

        Announcement ann1 = Announcement.builder().id(100L).build();
        Announcement ann2 = Announcement.builder().id(101L).build();
        Assignment as1 = Assignment.builder().id(200L).build();
        Resource res1 = Resource.builder().id(300L).build();

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.of(leader));
        given(announcementRepository.findAllByStudyId(studyId)).willReturn(List.of(ann1, ann2));
        given(assignmentRepository.findAllByStudyId(studyId)).willReturn(List.of(as1));
        given(resourceRepository.findAllByStudyId(studyId)).willReturn(List.of(res1));
        given(submissionRepository.findIdsByAssignmentId(as1.getId())).willReturn(List.of());

        // when
        studyService.deleteStudy(studyId, leaderUserId);

        // then
        then(fileService).should().deleteFilesByAnnouncementId(ann1.getId());
        then(fileService).should().deleteFilesByAnnouncementId(ann2.getId());
        then(fileService).should().deleteFilesByAssignmentId(as1.getId());
        then(fileService).should().deleteFilesByResourceId(res1.getId());
    }

    @Test
    @DisplayName("과제 제출물 파일 삭제 서비스 호출 검증")
    void deleteStudy_SubmissionFileServiceInvocation() {
        // given
        Long studyId = 1L;
        Long leaderUserId = 10L;
        StudyMember leader = StudyMember.builder()
                .user(User.builder().id(leaderUserId).build())
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();

        Assignment as1 = Assignment.builder().id(200L).build();
        List<Long> submissionIds = List.of(1000L, 1001L);

        given(studyMemberRepository.findByStudyIdAndRoleAndStatus(
                eq(studyId), eq(StudyRole.LEADER), eq(StudyMemberStatus.JOINED)))
                .willReturn(Optional.of(leader));
        given(announcementRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(assignmentRepository.findAllByStudyId(studyId)).willReturn(List.of(as1));
        given(resourceRepository.findAllByStudyId(studyId)).willReturn(List.of());
        given(submissionRepository.findIdsByAssignmentId(as1.getId())).willReturn(submissionIds);

        // when
        studyService.deleteStudy(studyId, leaderUserId);

        // then
        then(fileService).should().deleteFilesBySubmissionIds(submissionIds);
        then(fileService).should().deleteFilesByAssignmentId(as1.getId());
    }
}