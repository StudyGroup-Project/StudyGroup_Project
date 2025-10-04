package com.study.focus.study.studyDelete;

import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.FileService;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.*;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DeleteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private SubmissionRepository submissionRepository;
    // ... 필요한 다른 레포지토리들 주입
    @MockBean // FileService를 실제 Bean 대신 Mock 객체로 대체
    private FileService fileService;

    private User leaderUser;
    private User otherUser;
    private Study study;
    private StudyMember leaderMember;

    @BeforeEach
    void setUp() {
        leaderUser = userRepository.save(User.builder().build());
        otherUser = userRepository.save(User.builder().build());
        study = studyRepository.save(Study.builder().maxMemberCount(10).build());
        leaderMember = studyMemberRepository.save(StudyMember.builder()
                .study(study)
                .user(leaderUser)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());

        // @Transactional에 의해 각 테스트 후 롤백되므로 tearDown은 불필요할 수 있음
        // 하지만 명시적으로 데이터를 정리하고 싶다면 아래 tearDown 사용
    }

    @AfterEach
    void tearDown() {
        // 자식 -> 부모 순서로 삭제
        fileRepository.deleteAll();
        // ... (다른 자식 엔티티 deleteAll)
        announcementRepository.deleteAll();
        assignmentRepository.deleteAll();
        resourceRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("삭제 성공: 제출물이 포함된 과제와 공지가 있는 스터디 삭제 (204 No Content)")
    void deleteStudy_success_withAllChildrenAndSubmissions() throws Exception {
        // given: 스터디에 공지, 과제, 제출물이 모두 존재하는 상황
        var announcement = announcementRepository.save(Announcement.builder()
                .study(study).author(leaderMember).title("공지1").description("내용").build());
        var assignment = assignmentRepository.save(Assignment.builder()
                .study(study).creator(leaderMember).title("과제1").description("내용")
                .startAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusDays(1)).build());

        // 과제에 대한 제출물 생성
        var submission = submissionRepository.save(Submission.builder()
                .assignment(assignment).submitter(leaderMember).description("제출물 내용").build());

        entityManager.flush(); // DB에 즉시 반영

        // fileService의 모든 delete 메서드가 호출될 때 아무것도 하지 않도록 설정
        Mockito.doNothing().when(fileService).deleteFilesByAnnouncementId(Mockito.anyLong());
        Mockito.doNothing().when(fileService).deleteFilesBySubmissionIds(Mockito.anyList());
        Mockito.doNothing().when(fileService).deleteFilesByAssignmentId(Mockito.anyLong());
        Mockito.doNothing().when(fileService).deleteFilesByResourceId(Mockito.anyLong());

        // when & then: 스터디 삭제 API 호출
        mockMvc.perform(delete("/api/studies/{studyId}", study.getId())
                        .with(user(new CustomUserDetails(leaderUser.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // then: 서비스 로직에 따라 fileService의 메서드들이 호출되었는지 검증
        Mockito.verify(fileService, Mockito.times(1)).deleteFilesByAnnouncementId(announcement.getId());
        Mockito.verify(fileService, Mockito.times(1)).deleteFilesBySubmissionIds(List.of(submission.getId()));
        Mockito.verify(fileService, Mockito.times(1)).deleteFilesByAssignmentId(assignment.getId());

        // 스터디와 모든 자식 엔티티가 실제로 DB에서 삭제되었는지 확인
        Assertions.assertThat(studyRepository.findById(study.getId())).isEmpty();
        Assertions.assertThat(announcementRepository.count()).isZero();
        Assertions.assertThat(assignmentRepository.count()).isZero();
        Assertions.assertThat(submissionRepository.count()).isZero();
    }

    @Test
    @DisplayName("삭제 성공: 자식 엔티티가 없는 스터디 삭제 (204 No Content)")
    void deleteStudy_success_noChildren() throws Exception {
        // given: 자식 엔티티가 없는 깨끗한 스터디 (setUp에서 이미 생성됨)
        long studyId = study.getId();

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}", studyId)
                        .with(user(new CustomUserDetails(leaderUser.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(studyRepository.findById(studyId)).isEmpty();
    }

    @Test
    @DisplayName("삭제 실패: 방장이 아닌 사용자가 삭제 시도 (403 Forbidden)")
    void deleteStudy_fail_notLeader() throws Exception {
        // given: otherUser는 스터디 멤버가 아니거나, 일반 멤버인 상황
        long studyId = study.getId();
        long initialStudyCount = studyRepository.count();

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}", studyId)
                        .with(user(new CustomUserDetails(otherUser.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden()); // 혹은 권한 로직에 따라 400 Bad Request

        // 스터디가 삭제되지 않았는지 확인
        Assertions.assertThat(studyRepository.count()).isEqualTo(initialStudyCount);
        Assertions.assertThat(studyRepository.findById(studyId)).isPresent();
    }

    @Test
    @DisplayName("삭제 실패: 존재하지 않는 스터디 삭제 시도 (400 Bad Request)")
    void deleteStudy_fail_studyNotFound() throws Exception {
        // given
        long nonExistentStudyId = 9999L;
        long initialStudyCount = studyRepository.count();

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}", nonExistentStudyId)
                        .with(user(new CustomUserDetails(leaderUser.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // 혹은 isNotFound()

        // 아무것도 삭제되지 않았는지 확인
        Assertions.assertThat(studyRepository.count()).isEqualTo(initialStudyCount);
    }
}
