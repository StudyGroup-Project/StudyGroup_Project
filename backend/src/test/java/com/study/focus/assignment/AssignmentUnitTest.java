package com.study.focus.assignment;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.dto.AssignmentCreateRequestDTO;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.service.AssignmentService;
import com.study.focus.common.domain.File;
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

    @InjectMocks
    private AssignmentService assignmentService;

    private AssignmentCreateRequestDTO dto(LocalDateTime start, LocalDateTime due, String title, String desc, List<MultipartFile> files) {
        AssignmentCreateRequestDTO dto = new AssignmentCreateRequestDTO();
        ReflectionTestUtils.setField(dto, "title", title);
        ReflectionTestUtils.setField(dto, "description", desc);
        ReflectionTestUtils.setField(dto, "startAt", start);
        ReflectionTestUtils.setField(dto, "dueAt", due);
        ReflectionTestUtils.setField(dto, "files", files);
        return dto;
    }

    private StudyMember leaderOf(Study s) { return StudyMember.builder().study(s).role(StudyRole.LEADER).build(); }
    private StudyMember memberOf(Study s) { return StudyMember.builder().study(s).role(StudyRole.MEMBER).build(); }

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", files);

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", null);

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", null);

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", null);

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", null);

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
        AssignmentCreateRequestDTO req = dto(start, due, "t", "d", files);

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
}
