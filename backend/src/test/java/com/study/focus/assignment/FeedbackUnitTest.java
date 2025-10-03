package com.study.focus.assignment;

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
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

class SubmissionUnitTest {

    @InjectMocks
    private SubmissionService submissionService;

    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private FileRepository fileRepository;
    @Mock private S3Uploader s3Uploader;

    private final Long studyId = 1L;
    private final Long assignmentId = 10L;
    private final Long userId = 100L;

    private StudyMember submitter;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
                    // 핵심: 같은 인스턴스에 id 주입 후 그대로 반환
                    org.springframework.test.util.ReflectionTestUtils.setField(s, "id", 999L);
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

        // 실패 시 이하 호출이 없어야 안전
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
                    org.springframework.test.util.ReflectionTestUtils.setField(s, "id", 1000L);
                    return s;
                });

        // S3 meta (키/파일명/콘텐츠 타입/사이즈) - 네 DTO 필드명에 맞춤
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

        // 메타 생성이 각 파일에 대해 호출되었는지
        then(s3Uploader).should(times(1)).makeMetaData(f1);
        then(s3Uploader).should(times(1)).makeMetaData(f2);

        // 업로드 호출 검증(키/파일 순서 일치)
        then(s3Uploader).should(times(1))
                .uploadFiles(eq(List.of("k1", "k2")), eq(List.of(f1, f2)));

        // ✅ 캡처 대신 실제 호출 로그로 합산 (save 2번 or saveAll 1번 모두 커버)
        int saveCalls = (int) org.mockito.Mockito.mockingDetails(fileRepository).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("save"))
                .map(i -> i.getArgument(0))
                .filter(arg -> arg instanceof File)
                .count();

        int saveAllItemCount = org.mockito.Mockito.mockingDetails(fileRepository).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("saveAll"))
                .map(i -> i.getArgument(0))
                .mapToInt(arg -> (arg instanceof List<?> l) ? l.size() : 0)
                .sum();

        assertThat(saveCalls + saveAllItemCount).isEqualTo(2);
    }


}
