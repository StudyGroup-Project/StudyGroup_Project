package com.study.focus.assignment;

import com.study.focus.account.service.UserService;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.CreateSubmissionRequest;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.assignment.service.SubmissionService;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionUnitTest {

    @InjectMocks
    private SubmissionService submissionService;

    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private FileRepository fileRepository;
    @Mock private S3Uploader s3Uploader;
    @Mock private GroupService groupService;
    @Mock private UserService userService;

    private final Long submissionId = 777L;
    private final Long studyId = 1L;
    private final Long assignmentId = 10L;
    private final Long userId = 100L;

    private StudyMember submitter;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        Study study = Study.builder().id(studyId).build();

        submitter = StudyMember.builder()
                .id(200L)
                .study(study)
                .build();

        assignment = Assignment.builder()
                .id(assignmentId)
                .study(study)
                .dueAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    @DisplayName("성공: 파일 없이 제출")
    void submit_without_files_success() {
        // given
        CreateSubmissionRequest dto = new CreateSubmissionRequest();
        dto.setDescription("hello");

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(submitter));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId))
                .willReturn(Optional.of(assignment));
        given(submissionRepository.existsByAssignmentIdAndSubmitterId(assignmentId, submitter.getId()))
                .willReturn(false);
        given(submissionRepository.save(any(Submission.class)))
                .willAnswer(inv -> {
                    Submission s = inv.getArgument(0);
                    ReflectionTestUtils.setField(s, "id", 999L);
                    return s;
                });

        // when
        Long id = submissionService.submitSubmission(studyId, assignmentId, userId, dto);

        // then
        assertThat(id).isEqualTo(999L);
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
        then(fileRepository).should(never()).save(any(File.class));
        then(fileRepository).should(never()).saveAll(anyList());
        then(submissionRepository).should(times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("실패: 마감 이후 제출 금지")
    void submit_after_deadline_fails() {
        // given
        assignment = Assignment.builder()
                .id(assignmentId)
                .study(submitter.getStudy())
                .dueAt(LocalDateTime.now().minusMinutes(1))
                .build();

        CreateSubmissionRequest dto = new CreateSubmissionRequest();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(submitter));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId))
                .willReturn(Optional.of(assignment));

        // when/then
        assertThatThrownBy(() ->
                submissionService.submitSubmission(studyId, assignmentId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(submissionRepository).should(never()).save(any());
        then(fileRepository).should(never()).save(any());
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("실패: 이미 제출한 사용자")
    void submit_duplicate_same_user_fails() {
        // given
        CreateSubmissionRequest dto = new CreateSubmissionRequest();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(submitter));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId))
                .willReturn(Optional.of(assignment));
        given(submissionRepository.existsByAssignmentIdAndSubmitterId(assignmentId, submitter.getId()))
                .willReturn(true);

        // when/then
        assertThatThrownBy(() ->
                submissionService.submitSubmission(studyId, assignmentId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(submissionRepository).should(never()).save(any());
        then(fileRepository).should(never()).save(any());
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("성공: 파일과 함께 제출(S3 업로드 ok)")
    void submit_with_files_success() {
        // given
        MultipartFile f1 = mock(MultipartFile.class);
        MultipartFile f2 = mock(MultipartFile.class);

        CreateSubmissionRequest dto = new CreateSubmissionRequest();
        dto.setDescription("desc");
        dto.setFiles(List.of(f1, f2));

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(submitter));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId))
                .willReturn(Optional.of(assignment));
        given(submissionRepository.existsByAssignmentIdAndSubmitterId(assignmentId, submitter.getId()))
                .willReturn(false);
        given(submissionRepository.save(any(Submission.class)))
                .willAnswer(inv -> {
                    Submission s = inv.getArgument(0);
                    ReflectionTestUtils.setField(s, "id", 1000L);
                    return s;
                });

        FileDetailDto m1 = FileDetailDto.builder()
                .key("k1").originalFileName("a.txt").fileSize(10L).contentType("text/plain").build();
        FileDetailDto m2 = FileDetailDto.builder()
                .key("k2").originalFileName("b.txt").fileSize(20L).contentType("text/plain").build();

        given(s3Uploader.makeMetaData(f1)).willReturn(m1);
        given(s3Uploader.makeMetaData(f2)).willReturn(m2);

        // when
        Long id = submissionService.submitSubmission(studyId, assignmentId, userId, dto);

        // then
        assertThat(id).isEqualTo(1000L);

        then(s3Uploader).should(times(1)).makeMetaData(f1);
        then(s3Uploader).should(times(1)).makeMetaData(f2);
        then(s3Uploader).should(times(1))
                .uploadFiles(eq(List.of("k1", "k2")), eq(List.of(f1, f2)));

        // save(File) 2회 또는 saveAll([...]) 1회의 총 원소 수가 2인지 확인
        int saveCalls = (int) Mockito.mockingDetails(fileRepository).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("save"))
                .map(i -> i.getArgument(0))
                .filter(arg -> arg instanceof File)
                .count();

        int saveAllItemCount = Mockito.mockingDetails(fileRepository).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("saveAll"))
                .map(i -> i.getArgument(0))
                .mapToInt(arg -> (arg instanceof List<?> l) ? l.size() : 0)
                .sum();

        assertThat(saveCalls + saveAllItemCount).isEqualTo(2);
    }

    @Test
    @DisplayName("성공: 파일이 없는 경우 제출물 상세 가져오기")
    void getSubmissionDetail_success_without_files() {
        // given
        // submitter.user 주입
        var user = Mockito.mock(com.study.focus.account.domain.User.class);
        given(user.getId()).willReturn(500L);
        ReflectionTestUtils.setField(submitter, "user", user);

        // 제출물 엔티티(생성시간/설명 세팅)
        Submission sub = Submission.builder()
                .assignment(assignment)
                .submitter(submitter)
                .description("no files")
                .build();
        ReflectionTestUtils.setField(sub, "id", submissionId);

        // 그룹원 검증 통과
        given(groupService.memberValidation(studyId, userId)).willReturn(submitter);
        // 과제/제출물 조회 성공
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(sub));
        // 프로필 조회
        var profile = Mockito.mock(com.study.focus.account.dto.GetMyProfileResponse.class);
        given(profile.getNickname()).willReturn("Alice");
        given(userService.getMyProfile(500L)).willReturn(profile);
        // 파일 없음
        given(fileRepository.findAllBySubmissionId(submissionId)).willReturn(List.of());

        // when
        var resp = submissionService.getSubmissionDetail(studyId, assignmentId, submissionId, userId);

        // then
        assertThat(resp.getId()).isEqualTo(submissionId);
        assertThat(resp.getSubmitterName()).isEqualTo("Alice");
        assertThat(resp.getDescription()).isEqualTo("no files");
        assertThat(resp.getFiles()).isEmpty();

        then(groupService).should(times(1)).memberValidation(studyId, userId);
        then(assignmentRepository).should(times(1)).findByIdAndStudyId(assignmentId, studyId);
        then(submissionRepository).should(times(1)).findByIdAndAssignmentId(submissionId, assignmentId);
        then(userService).should(times(1)).getMyProfile(500L);
        then(fileRepository).should(times(1)).findAllBySubmissionId(submissionId);
    }

    @Test
    @DisplayName("성공: 파일이 있는 경우 제출물 상세 가져오기")
    void getSubmissionDetail_success_with_files() {
        // given
        // submitter.user 주입
        var user = Mockito.mock(com.study.focus.account.domain.User.class);
        given(user.getId()).willReturn(501L);
        ReflectionTestUtils.setField(submitter, "user", user);

        // 제출물 엔티티
        Submission sub = Submission.builder()
                .assignment(assignment)
                .submitter(submitter)
                .description("with files")
                .build();
        ReflectionTestUtils.setField(sub, "id", submissionId);

        given(groupService.memberValidation(studyId, userId)).willReturn(submitter);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(sub));
        var profile = Mockito.mock(com.study.focus.account.dto.GetMyProfileResponse.class);
        given(profile.getNickname()).willReturn("Bob");
        given(userService.getMyProfile(501L)).willReturn(profile);

        // 파일 2개(엔티티 목으로 fileKey 스텁)
        com.study.focus.common.domain.File f1 = Mockito.mock(com.study.focus.common.domain.File.class);
        com.study.focus.common.domain.File f2 = Mockito.mock(com.study.focus.common.domain.File.class);
        given(f1.getFileKey()).willReturn("k1");
        given(f2.getFileKey()).willReturn("k2");
        given(fileRepository.findAllBySubmissionId(submissionId)).willReturn(List.of(f1, f2));

        // when
        var resp = submissionService.getSubmissionDetail(studyId, assignmentId, submissionId, userId);

        // then
        assertThat(resp.getId()).isEqualTo(submissionId);
        assertThat(resp.getSubmitterName()).isEqualTo("Bob");
        assertThat(resp.getDescription()).isEqualTo("with files");
        assertThat(resp.getFiles()).hasSize(2);

        assertThat(resp.getFiles().stream().map(a -> a.getUrl()))
                .containsExactlyInAnyOrder("k1", "k2");

    }


    @Test
    @DisplayName("실패: studyId가 null인 경우")
    void getSubmissionDetail_fail_null_studyId() {
        // given: memberValidation에서 즉시 예외 발생하도록 스텁
        given(groupService.memberValidation(null, userId))
                .willThrow(new BusinessException(com.study.focus.common.exception.CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() ->
                submissionService.getSubmissionDetail(null, assignmentId, submissionId, userId)
        ).isInstanceOf(BusinessException.class);

        // 이후 의존성은 전혀 호출되지 않아야 함
        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(fileRepository).shouldHaveNoInteractions();
    }


    @Test
    @DisplayName("실패: userId가 null인 경우")
    void getSubmissionDetail_fail_null_userId() {
        // given: memberValidation에서 즉시 예외 발생하도록 스텁
        given(groupService.memberValidation(studyId, null))
                .willThrow(new BusinessException(com.study.focus.common.exception.CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() ->
                submissionService.getSubmissionDetail(studyId, assignmentId, submissionId, null)
        ).isInstanceOf(BusinessException.class);

        // 이후 의존성 호출 없음 검증(둘 중 하나 선택)
        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(fileRepository).shouldHaveNoInteractions();
    }


    @Test
    @DisplayName("실패: assignmentId가 null인 경우")
    void getSubmissionDetail_fail_null_assignmentId() {
        // groupService는 호출될 수 있으므로 기본 스텁(통과)
        given(groupService.memberValidation(studyId, userId)).willReturn(submitter);

        // when / then (assignmentRepository가 빈 Optional을 반환하여 BusinessException)
        assertThatThrownBy(() ->
                submissionService.getSubmissionDetail(studyId, null, submissionId, userId)
        ).isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(times(1)).findByIdAndStudyId(null, studyId);
        then(submissionRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(fileRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패: 그룹원이 아닌 사용자의 접근")
    void getSubmissionDetail_fail_user_not_group_member() {
        // given: 그룹 검증에서 예외 발생
        given(groupService.memberValidation(studyId, userId))
                .willThrow(new BusinessException(com.study.focus.common.exception.CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() ->
                submissionService.getSubmissionDetail(studyId, assignmentId, submissionId, userId)
        ).isInstanceOf(BusinessException.class);

        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(fileRepository).shouldHaveNoInteractions();
    }
}
