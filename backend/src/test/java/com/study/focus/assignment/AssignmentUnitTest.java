package com.study.focus.assignment;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.dto.*;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.assignment.service.AssignmentService;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.AssignmentFileResponse;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
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
    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private FileRepository fileRepository;
    @Mock private S3Uploader s3Uploader;
    @Mock private SubmissionRepository submissionRepository;

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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));

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
        then(studyMemberRepository).should(times(1)).findByStudyIdAndUserId(studyId, userId);
        then(assignmentRepository).should(times(1)).findAllByStudyIdOrderByCreatedAtDesc(studyId);
    }

    @DisplayName("성공: 과제가 없을 때 빈 목록 반환")
    @Test
    void getAssignments_success_emptyList() {
        // given
        Long studyId = 1L, userId = 100L;

        Study study = Study.builder().build();
        StudyMember member = StudyMember.builder().study(study).build();
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));

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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.empty());

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

        then(studyMemberRepository).should(never()).findByStudyIdAndUserId(any(), any());
        then(assignmentRepository).should(never()).findAllByStudyIdOrderByCreatedAtDesc(any());
    }

    @DisplayName("실패: userId가 null")
    @Test
    void getAssignments_fail_nullUserId() {
        Long studyId = 1L;

        assertThatThrownBy(() -> assignmentService.getAssignments(studyId, null))
                .isInstanceOf(BusinessException.class);

        then(studyMemberRepository).should(never()).findByStudyIdAndUserId(any(), any());
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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));

        // 파일 메타 생성 스텁(여러 번 호출되므로 answer로 처리)
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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
        given(assignmentRepository.save(any(Assignment.class)))
                .willReturn(Assignment.builder().id(1L).study(study).creator(leader).title("t").description("d").startAt(start).dueAt(due).build());

        // when
        assignmentService.createAssignment(studyId, userId, req);

        // then
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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));

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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.empty());

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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));

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
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));

        // 파일 메타 파싱에서 예외
        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3Uploader).makeMetaData(any(MultipartFile.class));

        // assignment 저장은 파일 처리 이전에 수행되므로 1회 호출됨
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
        // 저장은 1회 되었지만, 파일 저장/업로드는 수행되지 않음
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

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
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

        // 삭제할 파일 조회
        File del1 = mock(File.class);
        File del2 = mock(File.class);
        List<File> toDelete = List.of(del1, del2);

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
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
        // 삭제 관련
        then(fileRepository).should(times(1)).findAllById(deleteIds);
        // saveAll은 리스트 한 번으로 1회 호출이 맞음
        then(fileRepository).should(times(1)).saveAll(anyList());
        then(fileRepository).should(times(1)).flush();
        // 도메인 메서드 호출 여부(선택) — mock이라면 verify 가능
        verify(del1, times(1)).deleteAssignmentFile();
        verify(del2, times(1)).deleteAssignmentFile();

        // 추가 관련
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

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));

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

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.empty());

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

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        // when & then
        assertThrows(BusinessException.class,
                () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));

        // 과제 조회는 1회 일어남(never 아님)
        then(assignmentRepository).should(times(1)).findByIdAndStudyId(assignmentId, studyId);

        // 파일 관련 저장/업로드는 수행되지 않음
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

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));

        // 파일 메타에서 예외 발생
        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3Uploader).makeMetaData(any(MultipartFile.class));

        // when & then
        assertThrows(BusinessException.class, () -> assignmentService.updateAssignment(studyId, assignmentId, userId, dto));

        // 파일 저장/업로드는 수행되지 않음
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
        // 유닛테스트에선 자동 주입 안 되므로 직접 세팅
        org.springframework.test.util.ReflectionTestUtils.setField(assignment, "createdAt", created);

        // 파일 2개(mock)
        com.study.focus.common.domain.File f1 = mock(com.study.focus.common.domain.File.class);
        com.study.focus.common.domain.File f2 = mock(com.study.focus.common.domain.File.class);
        when(f1.getFileKey()).thenReturn("key-a");
        when(f2.getFileKey()).thenReturn("key-b");

        // submissions: 개수만 검증
        List<SubmissionListResponse> submissions = List.of(mock(SubmissionListResponse.class));

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));
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

        then(studyMemberRepository).should(times(1)).findByStudyIdAndUserId(studyId, userId);
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
        // 유닛 테스트에서는 createdAt 자동 주입이 안 되므로 필요 시 수동 세팅
        org.springframework.test.util.ReflectionTestUtils.setField(assignment, "createdAt", created);

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(member));
        given(assignmentRepository.findById(assignmentId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findSubmissionList(assignmentId)).willReturn(List.of());
        given(fileRepository.findAllByAssignmentId(assignmentId)).willReturn(List.of());

        // when
        GetAssignmentDetailResponse res = assignmentService.getAssignmentDetail(studyId, assignmentId, userId);

        // then
        assertThat(res.getFiles()).isEmpty();   // ← DTO 게터명에 맞춰 수정
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

        then(studyMemberRepository).should(never()).findByStudyIdAndUserId(any(), any());
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

        then(studyMemberRepository).should(never()).findByStudyIdAndUserId(any(), any());
        then(assignmentRepository).should(never()).findById(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
        then(submissionRepository).should(never()).findSubmissionList(any());
    }

    @Test
    @DisplayName("조회 실패: 스터디 멤버가 아님")
    void getAssignmentDetail_fail_notStudyMember() {
        // given
        Long studyId = 1L, assignmentId = 10L, userId = 100L;
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> assignmentService.getAssignmentDetail(studyId, assignmentId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(never()).findById(any());
        then(fileRepository).should(never()).findAllByAssignmentId(any());
        then(submissionRepository).should(never()).findSubmissionList(any());
    }

}
