package com.study.focus.assignment;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.dto.*;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.assignment.service.AssignmentService;
import com.study.focus.common.domain.File;
import com.study.focus.assignment.dto.AssignmentFileResponse;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.notification.service.NotificationService;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentUnitTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private StudyRepository studyRepository;
    @Mock private FileRepository fileRepository;
    @Mock private S3Uploader s3Uploader;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private GroupService groupService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private AssignmentService assignmentService;

    private CreateAssignmentRequest dto(LocalDateTime start, LocalDateTime due, String title, String desc, List<MultipartFile> files) {
        CreateAssignmentRequest dto = new CreateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", title);
        ReflectionTestUtils.setField(dto, "description", desc);
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);
        ReflectionTestUtils.setField(dto, "files", files);
        return dto;
    }

    private StudyMember leaderOf(Study s) { return StudyMember.builder().study(s).role(StudyRole.LEADER).build(); }
    private StudyMember memberOf(Study s) { return StudyMember.builder().study(s).role(StudyRole.MEMBER).build(); }

    /* 과제 제목 list 반환 테스트 */

    @DisplayName("성공: 과제가 있을 때 과제 목록 반환")
    @Test
    void getAssignments_success_withItems() {
        // given
        Long studyId = 1L, userId = 100L;

        Study study = Study.builder().build();
        StudyMember member = StudyMember.builder().study(study).build();
        given(groupService.memberValidation(studyId, userId)).willReturn(member);

        LocalDateTime now = LocalDateTime.now();
        Assignment a1 = Assignment.builder()
                .id(10L).study(study).creator(member)
                .title("T1").startAt(now).dueAt(now.plusDays(1)).build();
        Assignment a2 = Assignment.builder()
                .id(11L).study(study).creator(member)
                .title("T2").startAt(now).dueAt(now.plusDays(2)).build();

        given(assignmentRepository.findAllByStudyIdOrderByCreatedAtDesc(studyId))
                .willReturn(List.of(a1, a2));

        // when
        List<GetAssignmentsResponse> result = assignmentService.getAssignments(studyId, userId);

        // then
        assertThat(result).hasSize(2);
        then(groupService).should(times(1)).memberValidation(studyId, userId);
        then(assignmentRepository).should(times(1)).findAllByStudyIdOrderByCreatedAtDesc(studyId);
    }

    @DisplayName("성공: 과제가 없을 때 빈 목록 반환")
    @Test
    void getAssignments_success_emptyList() {
        // given
        Long studyId = 1L, userId = 100L;

        Study study = Study.builder().build();
        StudyMember member = StudyMember.builder().study(study).build();
        given(groupService.memberValidation(studyId, userId)).willReturn(member);

        given(assignmentRepository.findAllByStudyIdOrderByCreatedAtDesc(studyId))
                .willReturn(List.of());

        // when
        List<GetAssignmentsResponse> result = assignmentService.getAssignments(studyId, userId);

        // then
        assertThat(result).isEmpty();
        then(assignmentRepository).should(times(1)).findAllByStudyIdOrderByCreatedAtDesc(studyId);
    }

    @DisplayName("실패: 유저가 해당 스터디 멤버가 아님")
    @Test
    void getAssignments_fail_notStudyMember() {
        // given
        Long studyId = 1L, userId = 999L;
        given(groupService.memberValidation(studyId, userId))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // then
        assertThatThrownBy(() -> assignmentService.getAssignments(studyId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(never()).findAllByStudyIdOrderByCreatedAtDesc(any());
    }

    @DisplayName("실패: studyId가 null")
    @Test
    void getAssignments_fail_nullStudyId() {
        Long userId = 100L;

        assertThatThrownBy(() -> assignmentService.getAssignments(null, userId))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findAllByStudyIdOrderByCreatedAtDesc(any());
    }

    @DisplayName("실패: userId가 null")
    @Test
    void getAssignments_fail_nullUserId() {
        Long studyId = 1L;

        assertThatThrownBy(() -> assignmentService.getAssignments(studyId, null))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findAllByStudyIdOrderByCreatedAtDesc(any());
    }

    /* 과제 생성 기능 test */

    @Test
    @DisplayName("성공: 파일이 존재할 때 과제 생성")
    void createAssignment_success_withFiles() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();
        StudyMember leader = leaderOf(study);

        List<MultipartFile> files = List.of(
                new MockMultipartFile("files","a.jpg","image/jpeg","a".getBytes()),
                new MockMultipartFile("files","b.pdf","application/pdf","b".getBytes())
        );
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(7);
        CreateAssignmentRequest req = dto(start, due, "t", "d", files);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);

        when(s3Uploader.makeMetaData(any(MultipartFile.class)))
                .thenAnswer(inv -> {
                    MultipartFile f = inv.getArgument(0);
                    return new FileDetailDto(f.getOriginalFilename(), "key-" + f.getOriginalFilename(), f.getContentType(), f.getSize());
                });

        given(assignmentRepository.save(any(Assignment.class)))
                .willAnswer(inv -> {
                    Assignment a = inv.getArgument(0);
                    return Assignment.builder()
                            .id(1L)
                            .study(a.getStudy())
                            .creator(a.getCreator())
                            .title(a.getTitle())
                            .description(a.getDescription())
                            .startAt(a.getStartAt())
                            .dueAt(a.getDueAt())
                            .build();
                });

        // when
        assignmentService.createAssignment(studyId, userId, req);

        // then
        then(groupService).should(times(1)).memberValidation(studyId, userId);
        then(groupService).should(times(1)).isLeader(leader);
        then(assignmentRepository).should(times(1)).save(any(Assignment.class));
        then(s3Uploader).should(times(files.size())).makeMetaData(any(MultipartFile.class));
        then(fileRepository).should(times(files.size())).save(any(File.class));
        then(s3Uploader).should(times(1)).uploadFiles(anyList(), eq(files));
    }

    @Test
    @DisplayName("성공: 파일이 없을 때 과제 생성")
    void createAssignment_success_withoutFiles() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();
        StudyMember leader = leaderOf(study);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);
        CreateAssignmentRequest req = dto(start, due, "t", "d", null);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);

        given(assignmentRepository.save(any(Assignment.class)))
                .willReturn(Assignment.builder().id(1L).study(study).creator(leader).title("t").description("d").startAt(start).dueAt(due).build());

        // when
        assignmentService.createAssignment(studyId, userId, req);

        // then
        then(groupService).should(times(1)).isLeader(leader);
        then(assignmentRepository).should(times(1)).save(any(Assignment.class));
        then(fileRepository).should(never()).save(any(File.class));
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("실패: 방장이 아닌 경우")
    void createAssignment_fail_notLeader() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();
        StudyMember member = memberOf(study);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);
        CreateAssignmentRequest req = dto(start, due, "t", "d", null);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId)).willReturn(member);
        doThrow(new BusinessException(UserErrorCode.URL_FORBIDDEN))
                .when(groupService).isLeader(member);

        // then
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(studyId, userId, req));
        then(assignmentRepository).should(never()).save(any());
        then(fileRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 스터디 멤버가 아닌 경우")
    void createAssignment_fail_notStudyMember() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);
        CreateAssignmentRequest req = dto(start, due, "t", "d", null);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // then
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(studyId, userId, req));
        then(assignmentRepository).should(never()).save(any());
        then(fileRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 마감일이 시작일보다 이후가 아님")
    void createAssignment_fail_dueNotAfterStart() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();
        StudyMember leader = leaderOf(study);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start; // 동시간(또는 이전) → 실패
        CreateAssignmentRequest req = dto(start, due, "t", "d", null);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);

        // then
        assertThatThrownBy(() -> assignmentService.createAssignment(studyId, userId, req))
                .isInstanceOf(BusinessException.class);
        then(assignmentRepository).should(never()).save(any());
        then(fileRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 파일 형식이 잘못됨 (makeMetaData에서 예외)")
    void createAssignment_fail_invalidFileType() {
        // given
        Long studyId = 1L, userId = 10L;
        Study study = Study.builder().build();
        StudyMember leader = leaderOf(study);

        List<MultipartFile> files = List.of(
                new MockMultipartFile("files","evil.exe","application/octet-stream","x".getBytes())
        );
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);
        CreateAssignmentRequest req = dto(start, due, "t", "d", files);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);

        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3Uploader).makeMetaData(any(MultipartFile.class));

        given(assignmentRepository.save(any(Assignment.class)))
                .willAnswer(inv -> {
                    Assignment a = inv.getArgument(0);
                    return Assignment.builder().id(99L)
                            .study(a.getStudy()).creator(a.getCreator())
                            .title(a.getTitle()).description(a.getDescription())
                            .startAt(a.getStartAt()).dueAt(a.getDueAt())
                            .build();
                });

        // when & then
        BusinessException ex = assertThrows(BusinessException.class, () -> assignmentService.createAssignment(studyId, userId, req));
        then(assignmentRepository).should(times(1)).save(any(Assignment.class));
        then(fileRepository).should(times(0)).save(any(File.class));
        then(s3Uploader).should(times(0)).uploadFiles(anyList(), anyList());
    }

    /* 과제 수정 기능 test */

    @Test
    @DisplayName("수정 성공: 파일 변화 없이 과제 수정(제목/내용/기간만 변경)")
    void updateAssignment_success_withoutFileChanges() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "new title");
        ReflectionTestUtils.setField(dto, "description", "new desc");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);
        ReflectionTestUtils.setField(dto, "files", null);
        ReflectionTestUtils.setField(dto, "deleteFileIds", null);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(leader)
                .title("old").description("old")
                .startAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusDays(1))
                .build();

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        // when
        assignmentService.updateAssignment(studyId, assignmentId, userId, dto);

        // then
        then(fileRepository).should(never()).save(any(File.class));
        then(fileRepository).should(never()).saveAll(anyList());
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("수정 성공: 파일 삭제 + 파일 추가가 함께 있는 과제 수정")
    void updateAssignment_success_withFileDeleteAndAdd() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(7);

        List<Long> deleteIds = List.of(101L, 102L);
        List<MultipartFile> files = List.of(
                new MockMultipartFile("files","a.jpg","image/jpeg","a".getBytes()),
                new MockMultipartFile("files","b.pdf","application/pdf","b".getBytes())
        );

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "t");
        ReflectionTestUtils.setField(dto, "description", "d");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);
        ReflectionTestUtils.setField(dto, "files", files);
        ReflectionTestUtils.setField(dto, "deleteFileIds", deleteIds);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(leader)
                .title("old").description("old")
                .startAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusDays(1))
                .build();

        File del1 = mock(File.class);
        File del2 = mock(File.class);
        List<File> toDelete = List.of(del1, del2);

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(fileRepository.findAllById(deleteIds)).willReturn(toDelete);

        when(s3Uploader.makeMetaData(any(MultipartFile.class))).thenAnswer(inv -> {
            MultipartFile f = inv.getArgument(0);
            return new FileDetailDto(f.getOriginalFilename(), "key-" + f.getOriginalFilename(),
                    f.getContentType(), f.getSize());
        });

        // when
        assignmentService.updateAssignment(studyId, assignmentId, userId, dto);

        // then
        then(fileRepository).should(times(1)).findAllById(deleteIds);
        then(fileRepository).should(times(1)).saveAll(anyList());
        then(fileRepository).should(times(1)).flush();
        verify(del1, times(1)).deleteAssignmentFile();
        verify(del2, times(1)).deleteAssignmentFile();

        then(s3Uploader).should(times(files.size())).makeMetaData(any(MultipartFile.class));
        then(fileRepository).should(times(files.size())).save(any(File.class));
        then(s3Uploader).should(times(1)).uploadFiles(anyList(), eq(files));
    }

    @Test
    @DisplayName("수정 실패: 방장이 아닌 경우")
    void updateAssignment_fail_notLeader() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember member = StudyMember.builder().study(study).role(StudyRole.MEMBER).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "t");
        ReflectionTestUtils.setField(dto, "description", "d");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);

        given(groupService.memberValidation(studyId, userId)).willReturn(member);
        doThrow(new BusinessException(UserErrorCode.URL_FORBIDDEN))
                .when(groupService).isLeader(member);

        // then
        assertThrows(BusinessException.class, () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));
        then(assignmentRepository).should(never()).findByIdAndStudyId(any(), any());
        then(fileRepository).should(never()).save(any());
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("수정 실패: 스터디 멤버가 아닌 경우")
    void updateAssignment_fail_notStudyMember() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(3);

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "t");
        ReflectionTestUtils.setField(dto, "description", "d");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);

        given(groupService.memberValidation(studyId, userId))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // then
        assertThrows(BusinessException.class, () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));
        then(assignmentRepository).should(never()).findByIdAndStudyId(any(), any());
    }

    @Test
    @DisplayName("수정 실패: 마감일이 시작일 이후가 아님")
    void updateAssignment_fail_dueNotAfterStart() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start; // 동시간 → 실패

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "t");
        ReflectionTestUtils.setField(dto, "description", "d");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(leader)
                .title("old").description("old")
                .startAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusDays(1))
                .build();

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        // when & then
        assertThrows(BusinessException.class,
                () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));

        then(assignmentRepository).should(times(1)).findByIdAndStudyId(assignmentId, studyId);
        then(fileRepository).should(never()).save(any());
        then(fileRepository).should(never()).saveAll(anyList());
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("수정 실패: 추가 파일의 형식이 잘못됨(makeMetaData에서 예외)")
    void updateAssignment_fail_invalidFileType_onAdd() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime due = start.plusDays(7);

        List<MultipartFile> files = List.of(
                new MockMultipartFile("files","evil.exe","application/octet-stream","x".getBytes())
        );

        UpdateAssignmentRequest dto = new UpdateAssignmentRequest();
        ReflectionTestUtils.setField(dto, "title", "t");
        ReflectionTestUtils.setField(dto, "description", "d");
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);
        ReflectionTestUtils.setField(dto, "files", files);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(leader)
                .title("old").description("old")
                .startAt(LocalDateTime.now()).dueAt(LocalDateTime.now().plusDays(1))
                .build();

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3Uploader).makeMetaData(any(MultipartFile.class));

        // when & then
        assertThrows(BusinessException.class, () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));

        then(fileRepository).should(never()).save(any(File.class));
        then(s3Uploader).should(never()).uploadFiles(anyList(), anyList());
    }

    /* 과제 상세보기 기능 test */

    @Test
    @DisplayName("조회 성공: 파일이 있을 때 과제 상세 조회")
    void getAssignmentDetail_success_withFiles() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().build();
        StudyMember member = StudyMember.builder().study(study).build();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime due = start.plusDays(7);
        LocalDateTime created = LocalDateTime.now().minusDays(2);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(member)
                .title("T").description("D")
                .startAt(start).dueAt(due)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(assignment, "createdAt", created);

        File f1 = mock(File.class);
        File f2 = mock(File.class);
        when(f1.getFileKey()).thenReturn("key-a");
        when(f2.getFileKey()).thenReturn("key-b");

        List<SubmissionListResponse> submissions = List.of(mock(SubmissionListResponse.class));

        given(groupService.memberValidation(studyId, userId)).willReturn(member);
        given(assignmentRepository.findById(assignmentId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findSubmissionList(assignmentId)).willReturn(submissions);
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of(f1, f2));

        // when
        GetAssignmentDetailResponse res = assignmentService.getAssignmentDetail(studyId, assignmentId, userId);

        // then
        assertThat(res.getId()).isEqualTo(assignmentId);
        assertThat(res.getTitle()).isEqualTo("T");
        assertThat(res.getDescription()).isEqualTo("D");
        assertThat(res.getStartAt()).isEqualTo(start);
        assertThat(res.getDueAt()).isEqualTo(due);
        assertThat(res.getCreateAt()).isEqualTo(created);
        assertThat(res.getFiles()).hasSize(2);
        assertThat(res.getFiles().stream().map(AssignmentFileResponse::getUrl))
                .containsExactlyInAnyOrder("key-a", "key-b");
        assertThat(res.getSubmissions()).hasSize(1);

        then(groupService).should(times(1)).memberValidation(studyId, userId);
        then(assignmentRepository).should(times(1)).findById(assignmentId);
        then(fileRepository).should(times(1)).findAllByAssignmentId(assignmentId);
        then(submissionRepository).should(times(1)).findSubmissionList(assignmentId);
    }

    @Test
    @DisplayName("조회 성공: 파일이 없을 때 과제 상세 조회(빈 리스트)")
    void getAssignmentDetail_success_withoutFiles() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().build();
        StudyMember member = StudyMember.builder().study(study).build();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime due = start.plusDays(3);
        LocalDateTime created = LocalDateTime.now().minusDays(2);

        Assignment assignment = Assignment.builder()
                .id(assignmentId).study(study).creator(member)
                .title("T2").description("D2")
                .startAt(start).dueAt(due)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(assignment, "createdAt", created);

        given(groupService.memberValidation(studyId, userId)).willReturn(member);
        given(assignmentRepository.findById(assignmentId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findSubmissionList(assignmentId)).willReturn(List.of());
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());

        // when
        GetAssignmentDetailResponse res = assignmentService.getAssignmentDetail(studyId, assignmentId, userId);

        // then
        assertThat(res.getFiles()).isEmpty();
        assertThat(res.getSubmissions()).isEmpty();
        then(fileRepository).should(times(1)).findAllByAssignmentId(assignmentId);
    }


    @Test
    @DisplayName("조회 실패: studyId가 null")
    void getAssignmentDetail_fail_nullStudyId() {
        // given
        Long assignmentId = 10L, userId = 100L;

        // then
        assertThatThrownBy(() -> assignmentService.getAssignmentDetail(null, assignmentId, userId))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findById(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
        then(submissionRepository).should(never()).findSubmissionList(any());
    }

    @Test
    @DisplayName("조회 실패: userId가 null")
    void getAssignmentDetail_fail_nullUserId() {
        // given
        Long studyId = 1L, assignmentId = 10L;

        // then
        assertThatThrownBy(() -> assignmentService.getAssignmentDetail(studyId, assignmentId, null))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findById(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
        then(submissionRepository).should(never()).findSubmissionList(any());
    }

    @Test
    @DisplayName("조회 실패: 스터디 멤버가 아님")
    void getAssignmentDetail_fail_notStudyMember() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;
        given(groupService.memberValidation(studyId, userId))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // then
        assertThatThrownBy(() -> assignmentService.getAssignmentDetail(studyId, assignmentId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(never()).findById(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
        then(submissionRepository).should(never()).findSubmissionList(any());
    }

    /* 과제 삭제 기능 test */

    @DisplayName("삭제 성공: 파일이 존재하고 제출물이 없는 과제 삭제")
    @Test
    void deleteAssignment_success_withAssignmentFiles_noSubmissions() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();
        Assignment assignment = Assignment.builder().id(assignmentId).study(study).creator(leader).build();

        File f1 = mock(File.class);
        File f2 = mock(File.class);

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        given(submissionRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of(f1, f2));

        // when
        assignmentService.deleteAssignment(studyId, assignmentId, userId);

        // then
        then(submissionRepository).should(times(1)).findAllByAssignmentId(assignmentId);
        then(fileRepository).should(times(1)).findAllByAssignmentId(assignmentId);
        verify(f1, times(1)).deleteAssignmentFile();
        verify(f2, times(1)).deleteAssignmentFile();
        then(fileRepository).should(times(1)).saveAll(anyList());
        then(assignmentRepository).should(times(1)).delete(assignment);
    }

    @DisplayName("삭제 성공: 파일이 존재하지 않고 제출물도 없는 과제 삭제")
    @Test
    void deleteAssignment_success_noFiles_noSubmissions() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();
        Assignment assignment = Assignment.builder().id(assignmentId).study(study).creator(leader).build();

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        given(submissionRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());

        // when
        assignmentService.deleteAssignment(studyId, assignmentId, userId);

        // then
        then(fileRepository).should(times(1)).findAllByAssignmentId(assignmentId);
        then(fileRepository).should(never()).save(any());
        then(fileRepository).should(never()).saveAll(anyList());
        then(submissionRepository).should(times(1)).findAllByAssignmentId(assignmentId);
        then(submissionRepository).should(never()).deleteAllInBatch(anyList());
        then(assignmentRepository).should(times(1)).delete(assignment);
    }

    @DisplayName("삭제 성공: 파일이 존재하지 않고 제출물이 있는 과제 삭제")
    @Test
    void deleteAssignment_success_withSubmissions_noFiles() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().study(study).role(StudyRole.LEADER).build();
        Assignment assignment = Assignment.builder().id(assignmentId).study(study).creator(leader).build();

        com.study.focus.assignment.domain.Submission s1 = mock(com.study.focus.assignment.domain.Submission.class);
        com.study.focus.assignment.domain.Submission s2 = mock(com.study.focus.assignment.domain.Submission.class);
        when(s1.getId()).thenReturn(201L);
        when(s2.getId()).thenReturn(202L);

        given(groupService.memberValidation(studyId, userId)).willReturn(leader);
        doNothing().when(groupService).isLeader(leader);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        given(submissionRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of(s1, s2));

        given(fileRepository.findAllBySubmissionIdIn(List.of(201L, 202L))).willReturn(List.of());
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());

        // when
        assignmentService.deleteAssignment(studyId, assignmentId, userId);

        // then
        then(fileRepository).should(times(1)).findAllBySubmissionIdIn(List.of(201L, 202L));
        then(fileRepository).should(never()).saveAll(anyList());
        then(submissionRepository).should(times(1)).deleteAll(List.of(s1, s2));
        then(assignmentRepository).should(times(1)).delete(assignment);
    }

    @DisplayName("삭제 실패: studyId가 null")
    @Test
    void deleteAssignment_fail_nullStudyId() {
        Long assignmentId = 10L, userId = 100L;

        assertThatThrownBy(() -> assignmentService.deleteAssignment(null, assignmentId, userId))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findByIdAndStudyId(any(), any());
        then(submissionRepository).should(never()).findAllByAssignmentId(any());
    }

    @DisplayName("삭제 실패: userId가 null")
    @Test
    void deleteAssignment_fail_nullUserId() {
        Long studyId = 1L, assignmentId = 10L;

        assertThatThrownBy(() -> assignmentService.deleteAssignment(studyId, assignmentId, null))
                .isInstanceOf(BusinessException.class);

        then(groupService).should(never()).memberValidation(any(), any());
        then(assignmentRepository).should(never()).findByIdAndStudyId(any(), any());
        then(submissionRepository).should(never()).findAllByAssignmentId(any());
    }

    @DisplayName("삭제 실패: 방장이 아닌 경우")
    @Test
    void deleteAssignment_fail_notLeader() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;

        Study study = Study.builder().id(studyId).build();
        StudyMember member = StudyMember.builder().study(study).role(StudyRole.MEMBER).build();

        given(groupService.memberValidation(studyId, userId)).willReturn(member);
        doThrow(new BusinessException(UserErrorCode.URL_FORBIDDEN))
                .when(groupService).isLeader(member);

        // then
        assertThatThrownBy(() -> assignmentService.deleteAssignment(studyId, assignmentId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(never()).findByIdAndStudyId(any(), any());
        then(submissionRepository).should(never()).findAllByAssignmentId(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
    }
}
