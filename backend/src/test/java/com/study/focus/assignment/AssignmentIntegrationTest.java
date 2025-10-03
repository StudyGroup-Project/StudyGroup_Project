package com.study.focus.assignment;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssignmentIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private SubmissionRepository submissionRepository;

    // 외부 I/O 막기 위해 테스트에서 S3Uploader를 목으로 대체
    @MockBean private S3Uploader s3Uploader;

    private Study study1;       // user1=LEADER
    private Study study2;       // user1=MEMBER, user2=LEADER
    private User user1;
    private User user2;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        // 기본 데이터
        user1 = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());
        user2 = userRepository.save(User.builder().trustScore(100L).lastLoginAt(LocalDateTime.now()).build());

        study1 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());
        study2 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());

        // user1: study1 리더
        StudyMember studyMember1 = studyMemberRepository.save(StudyMember.builder()
                .user(user1).study(study1)
                .role(StudyRole.LEADER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());
        // user1: study2 멤버
        StudyMember studyMember2 = studyMemberRepository.save(StudyMember.builder()
                .user(user1).study(study2)
                .role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());
        // user2: study2 리더
        StudyMember studyMember3 = studyMemberRepository.save(StudyMember.builder()
                .user(user2).study(study2)
                .role(StudyRole.LEADER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());

        LocalDateTime now = LocalDateTime.now();

        assignmentRepository.save(Assignment.builder().study(study1).creator(studyMember1).title("1").startAt(now.minusDays(1))
                .dueAt(now.plusDays(7)).build());
        assignmentRepository.save(Assignment.builder().study(study1).creator(studyMember1).title("2").startAt(now.minusHours(1))
                .dueAt(now.plusDays(10)).build());
    }

    @AfterEach
    void tearDown() {
        fileRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String iso(LocalDateTime t) {
        return t.withNano(0).format(ISO); // ModelAttribute 바인딩 기본 ISO 형식
    }

    /* 과제 목록 조회 기능 test */

    @Test
    @DisplayName("성공: 스터디 멤버가 과제 목록을 성공적으로 조회")
    void getAssignment_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/studies/" + study1.getId() + "/assignments")
                        .with(user(new CustomUserDetails(user1.getId())))) // setUp에서 생성된 user를 사용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title").value(containsInAnyOrder("1", "2")));
    }


    @Test
    @DisplayName("실패: 스터디 멤버가 아닐 경우 IllegalArgumentException이 발생")
    void getAssignment_Fail_NotStudyMember() throws Exception {
        mockMvc.perform(get("/api/studies/" + study1.getId() + "/assignments")
                        .with(user(new CustomUserDetails(user2.getId()))))
                .andExpect(status().isBadRequest());
    }

    /* 과제 생성 기능 test */

    @Test
    @DisplayName("성공: 파일이 존재할 때 과제 생성 (201 Created + Location)")
    void createAssignment_success_withFile() throws Exception {
        long before = assignmentRepository.count();

        // 파일 메타 생성/업로드 목 동작
        when(s3Uploader.makeMetaData(any())).thenAnswer(inv -> {
            MockMultipartFile f = inv.getArgument(0);
            return new FileDetailDto(f.getOriginalFilename(), "key-" + f.getOriginalFilename(), f.getContentType(), f.getSize());
        });
        doNothing().when(s3Uploader).uploadFiles(anyList(), anyList());

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start.plusDays(5);

        MockMultipartFile file = new MockMultipartFile(
                "files", "hello.pdf", "application/pdf", "PDFDATA".getBytes()
        );

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .file(file)
                        .param("title", "Assignment 1")
                        .param("description", "Do it!")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesRegex(
                        "/api/studies/" + study1.getId() + "/assignments/\\d+"
                )));

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before + 1);
        // 파일 메타 생성/업로드 호출 검증은 단위 테스트에서 충분히 했으므로 여기서는 최소 보장만
        verify(s3Uploader, atLeastOnce()).makeMetaData(any());
        verify(s3Uploader, times(1)).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("성공: 파일이 없을 때 과제 생성 (201 Created)")
    void createAssignment_success_withoutFile() throws Exception {
        long beforeAssignments = assignmentRepository.count();
        long beforeFiles = fileRepository.count();

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start.plusDays(3);

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .param("title", "Assignment 2")
                        .param("description", "No file")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(beforeAssignments + 1);
        Assertions.assertThat(fileRepository.count()).isEqualTo(beforeFiles);
        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("실패: 방장이 아닌 경우 (403 Forbidden)")
    void createAssignment_fail_notLeader() throws Exception {
        long before = assignmentRepository.count();

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start.plusDays(3);

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study2.getId()) // study2에서 user1은 MEMBER
                        .param("title", "Not leader")
                        .param("description", "forbidden")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("실패: 그룹 미소속(스터디 멤버 아님) (400 Bad Request)")
    void createAssignment_fail_notStudyMember() throws Exception {
        long before = assignmentRepository.count();

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start.plusDays(3);

        // user2는 study1에 소속되지 않음
        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .param("title", "no member")
                        .param("description", "bad request")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user2.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("실패: 마감일이 시작일보다 이후가 아님 (400 Bad Request)")
    void createAssignment_fail_dueNotAfterStart() throws Exception {
        long before = assignmentRepository.count();

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start; // 동일 → 실패 조건

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .param("title", "bad due")
                        .param("description", "invalid range")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("실패: 파일 형식이 잘못됨 (400 Bad Request, 트랜잭션 롤백)")
    void createAssignment_fail_invalidFileType() throws Exception {
        long before = assignmentRepository.count();

        // s3 메타 생성에서 예외 던지기
        when(s3Uploader.makeMetaData(any())).thenThrow(
                new com.study.focus.common.exception.BusinessException(
                        com.study.focus.common.exception.UserErrorCode.INVALID_FILE_TYPE
                )
        );

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start.plusDays(2);

        MockMultipartFile bad = new MockMultipartFile(
                "files", "evil.exe", "application/octet-stream", "BAD".getBytes()
        );

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .file(bad)
                        .param("title", "invalid file")
                        .param("description", "should fail")
                        .param("startAt", iso(start))
                        .param("dueAt", iso(due))
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // @Transactional 롤백으로 과제 생성되지 않아야 함
        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before);
        verify(s3Uploader, times(1)).makeMetaData(any());
        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    // (옵션) 바인딩 실패 케이스: 포맷이 완전히 잘못된 시간 문자열이면 스프링 바인딩 단계에서 400이 나올 수 있음.
    @Test
    @DisplayName("실패(옵션): 잘못된 날짜 포맷으로 인한 바인딩 오류 (400 Bad Request)")
    void createAssignment_fail_invalidDateFormat_binding() throws Exception {
        long before = assignmentRepository.count();

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments", study1.getId())
                        .param("title", "date binding fail")
                        .param("description", "bad date")
                        .param("startAt", "2025/09/27 10:00") // 잘못된 포맷
                        .param("dueAt", "not-a-date")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // @ControllerAdvice에 따라 400 매핑 가정

        Assertions.assertThat(assignmentRepository.count()).isEqualTo(before);
    }

    private MockMultipartHttpServletRequestBuilder putMultipart(String urlTemplate, Object... uriVars) {
        return multipart(HttpMethod.PUT, urlTemplate, uriVars);
    }

    /* 과제 수정 기능 test */

    @Test
    @DisplayName("수정 성공: 파일 변화 없이 과제 수정")
    void updateAssignment_success_withoutFileChanges() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("old").description("old")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime newDue = newStart.plusDays(3);

        // when & then
        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                                .param("title", "new title")
                                .param("description", "new desc")
                                .param("startAt", iso(newStart))
                                .param("dueAt", iso(newDue))
                                .with(user(new CustomUserDetails(user1.getId())))
                                .with(csrf())
                )
                .andExpect(status().isOk());

        // verify DB changed
        var updated = assignmentRepository.findById(a.getId()).orElseThrow();
        Assertions.assertThat(updated.getTitle()).isEqualTo("new title");
        Assertions.assertThat(updated.getDescription()).isEqualTo("new desc");
        Assertions.assertThat(updated.getStartAt().withNano(0)).isEqualTo(newStart);
        Assertions.assertThat(updated.getDueAt().withNano(0)).isEqualTo(newDue);

        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("수정 성공: 파일 삭제 + 파일 추가가 함께 있는 과제 수정")
    void updateAssignment_success_withFileDeleteAndAdd() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        // 기존 첨부 2개 심기 (삭제 대상)
        var meta1 = new FileDetailDto("a.pdf", "key-a", "application/pdf", 10);
        var meta2 = new FileDetailDto("b.jpg", "key-b", "image/jpeg", 20);
        var f1 = fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta1));
        var f2 = fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta2));

        long beforeFiles = fileRepository.count();

        // 추가할 파일 2개
        var up1 = new MockMultipartFile("files", "c.pdf", "application/pdf", "C".getBytes());
        var up2 = new MockMultipartFile("files", "d.png", "image/png", "D".getBytes());

        // S3 메타 생성/업로드 목
        when(s3Uploader.makeMetaData(any())).thenAnswer(inv -> {
            MockMultipartFile f = inv.getArgument(0);
            return new FileDetailDto(f.getOriginalFilename(), "key-" + f.getOriginalFilename(), f.getContentType(), f.getSize());
        });
        doNothing().when(s3Uploader).uploadFiles(anyList(), anyList());

        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime newDue = newStart.plusDays(7);

        // when & then
        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                                .file(up1)
                                .file(up2)
                                .param("title", "t2")
                                .param("description", "d2")
                                .param("startAt", iso(newStart))
                                .param("dueAt", iso(newDue))
                                .param("deleteFileIds", String.valueOf(f1.getId()), String.valueOf(f2.getId()))
                                .with(user(new CustomUserDetails(user1.getId())))
                                .with(csrf())
                )
                .andExpect(status().isOk());

        // 파일 개수: 소프트 삭제 가정 → 총 행수는 +2(추가분) 증가
        Assertions.assertThat(fileRepository.count()).isEqualTo(beforeFiles + 2);
        verify(s3Uploader, times(2)).makeMetaData(any());
        verify(s3Uploader, times(1)).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("수정 실패: 방장이 아닌 경우 (403 Forbidden)")
    void updateAssignment_fail_notLeader() throws Exception {
        // user1은 study2에서 MEMBER, 과제는 study2 리더(user2)가 생성
        var leader2 = studyMemberRepository.findByStudyIdAndUserId(study2.getId(), user2.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study2).creator(leader2)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime newDue = newStart.plusDays(3);

        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study2.getId(), a.getId())
                                .param("title", "x")
                                .param("description", "y")
                                .param("startAt", iso(newStart))
                                .param("dueAt", iso(newDue))
                                .with(user(new CustomUserDetails(user1.getId()))) // MEMBER
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("수정 실패: 스터디원이 아닌 경우 (400 Bad Request)")
    void updateAssignment_fail_notStudyMember() throws Exception {
        // 과제는 study1 (user1 리더), 요청자는 user2(미소속)
        var leader1 = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader1)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime newDue = newStart.plusDays(3);

        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                                .param("title", "x")
                                .param("description", "y")
                                .param("startAt", iso(newStart))
                                .param("dueAt", iso(newDue))
                                .with(user(new CustomUserDetails(user2.getId()))) // study1 미소속
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수정 실패: 마감일이 시작일 이후가 아님 (400 Bad Request)")
    void updateAssignment_fail_dueNotAfterStart() throws Exception {
        var leader1 = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader1)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime due = start; // 동일 → 실패

        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                                .param("title", "bad")
                                .param("description", "range")
                                .param("startAt", iso(start))
                                .param("dueAt", iso(due))
                                .with(user(new CustomUserDetails(user1.getId())))
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());

        // 내용 미변경 확인
        var after = assignmentRepository.findById(a.getId()).orElseThrow();
        Assertions.assertThat(after.getTitle()).isEqualTo("t");
        Assertions.assertThat(after.getDescription()).isEqualTo("d");
    }

    @Test
    @DisplayName("수정 실패: 추가 파일 형식이 잘못됨 (400 Bad Request, 트랜잭션 롤백)")
    void updateAssignment_fail_invalidFileType_onAdd() throws Exception {
        var leader1 = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader1)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(5).withNano(0))
                .build());

        // s3 메타 생성에서 예외
        when(s3Uploader.makeMetaData(any())).thenThrow(
                new com.study.focus.common.exception.BusinessException(
                        com.study.focus.common.exception.UserErrorCode.INVALID_FILE_TYPE
                )
        );

        LocalDateTime newStart = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime newDue = newStart.plusDays(3);

        var bad = new MockMultipartFile("files", "evil.exe", "application/octet-stream", "BAD".getBytes());

        long beforeFiles = fileRepository.count();

        mockMvc.perform(
                        putMultipart("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                                .file(bad)
                                .param("title", "x")
                                .param("description", "y")
                                .param("startAt", iso(newStart))
                                .param("dueAt", iso(newDue))
                                .with(user(new CustomUserDetails(user1.getId())))
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());

        // 파일/메타 추가되지 않음
        Assertions.assertThat(fileRepository.count()).isEqualTo(beforeFiles);
        verify(s3Uploader, times(1)).makeMetaData(any());
        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    /* 과제 상세보기 기능 test */

    @Test
    @DisplayName("조회 성공: 파일이 있을 때 과제 상세 조회")
    void getAssignmentDetail_success_withFiles_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();

        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("detail t").description("detail d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(3).withNano(0))
                .build());

        // 첨부 파일 2개
        var meta1 = new FileDetailDto("a.pdf", "key-a", "application/pdf", 10);
        var meta2 = new FileDetailDto("b.png", "key-b", "image/png", 20);
        fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta1));
        fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta2));

        // when & then
        mockMvc.perform(get("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(a.getId()))
                .andExpect(jsonPath("$.title").value("detail t"))
                .andExpect(jsonPath("$.description").value("detail d"))
                .andExpect(jsonPath("$.files.length()").value(2))
                .andExpect(jsonPath("$.files[*].url").value(org.hamcrest.Matchers.containsInAnyOrder("key-a", "key-b")))
                .andExpect(jsonPath("$.submissions.length()").value(0)); // 별도 제출 데이터 없으므로 0 가정
    }

    // 성공: 파일이 없을 때 과제 상세 조회(빈 리스트)
    @Test
    @DisplayName("조회 성공: 파일이 없을 때 과제 상세 조회(빈 리스트)")
    void getAssignmentDetail_success_withoutFiles_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();

        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("no files").description("none")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(2).withNano(0))
                .build());

        // when & then
        mockMvc.perform(get("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(a.getId()))
                .andExpect(jsonPath("$.files.length()").value(0))
                .andExpect(jsonPath("$.submissions.length()").value(0));
    }

    // 실패: 스터디 멤버가 아닌 경우
    @Test
    @DisplayName("조회 실패: 스터디 멤버가 아님 (400 Bad Request)")
    void getAssignmentDetail_fail_notStudyMember_it() throws Exception {
        // given: study1에는 user2 미소속(세팅에 따라 다르면 보정)
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(1).withNano(0))
                .build());

        // when & then
        mockMvc.perform(get("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user2.getId())))) // study1 미소속
                .andExpect(status().isBadRequest());
    }

    // 실패: 인증 주체 없음(=userId 없음에 준함) → 보안 설정에 따라 401 Unauthorized 예상
    @Test
    @DisplayName("조회 실패: 인증되지 않은 사용자 (401 Unauthorized)")
    void getAssignmentDetail_fail_unauthenticated_it() throws Exception {
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("t").description("d")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(1).withNano(0))
                .build());

        mockMvc.perform(get("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId()))
                .andExpect(status().isUnauthorized()); // 스프링 시큐리티 기본 정책 가정
    }


    @Test
    @DisplayName("삭제 성공: 파일이 존재하고 제출물이 없는 과제 삭제 (204 No Content)")
    void deleteAssignment_success_withAssignmentFiles_noSubmissions_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("del t1").description("del d1")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(3).withNano(0))
                .build());

        // 과제 직결 파일 2개
        var meta1 = new FileDetailDto("a.pdf", "key-a", "application/pdf", 10);
        var meta2 = new FileDetailDto("b.png", "key-b", "image/png", 20);
        fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta1));
        fileRepository.save(com.study.focus.common.domain.File.ofAssignment(a, meta2));

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 과제 삭제 확인
        Assertions.assertThat(assignmentRepository.findById(a.getId())).isEmpty();
        // FK 해제로 인해 과제 기준 조회 시 0 기대(soft delete/연관 끊김 가정)
        Assertions.assertThat(fileRepository.findAllByAssignmentId(a.getId())).isEmpty();
    }

    @Test
    @DisplayName("삭제 성공: 파일/제출물이 모두 없는 과제 삭제 (204 No Content)")
    void deleteAssignment_success_noFiles_noSubmissions_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("del t2").description("del d2")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(2).withNano(0))
                .build());

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(assignmentRepository.findById(a.getId())).isEmpty();
    }

    @Test
    @DisplayName("삭제 성공: 제출물이 있고 파일은 없는 과제 삭제 (204 No Content)")
    void deleteAssignment_success_withSubmissions_noFiles_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("del t3").description("del d3")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(4).withNano(0))
                .build());

        // 제출물 2개 (필드명/생성자는 프로젝트 엔티티에 맞게 조정)
        var s1 = submissionRepository.save(com.study.focus.assignment.domain.Submission.builder()
                .assignment(a).submitter(leader).build());
        var s2 = submissionRepository.save(com.study.focus.assignment.domain.Submission.builder()
                .assignment(a).submitter(leader).build());

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 과제 삭제 + 제출물 삭제 확인
        Assertions.assertThat(assignmentRepository.findById(a.getId())).isEmpty();
        Assertions.assertThat(submissionRepository.findAllByAssignmentId(a.getId())).isEmpty();
        // 과제 직결 파일 없음 가정이므로 별도 검증 불필요
    }

    @Test
    @DisplayName("삭제 실패: 방장이 아닌 경우 (403 Forbidden)")
    void deleteAssignment_fail_notLeader_it() throws Exception {
        // given: study2의 리더는 user2, user1은 MEMBER
        var leader2 = studyMemberRepository.findByStudyIdAndUserId(study2.getId(), user2.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study2).creator(leader2)
                .title("forbidden del").description("x")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(3).withNano(0))
                .build());

        // when & then: MEMBER(user1)로 삭제 시도
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study2.getId(), a.getId())
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // 여전히 존재
        Assertions.assertThat(assignmentRepository.findById(a.getId())).isPresent();
    }

    @Test
    @DisplayName("삭제 실패: 스터디 멤버가 아닌 경우 (400 Bad Request)")
    void deleteAssignment_fail_notStudyMember_it() throws Exception {
        // given: study1의 리더는 user1, user2는 미소속
        var leader1 = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader1)
                .title("bad request del").description("x")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(3).withNano(0))
                .build());

        // when & then: study1 미소속 user2로 삭제 시도
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(user(new CustomUserDetails(user2.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(assignmentRepository.findById(a.getId())).isPresent();
    }

    @Test
    @DisplayName("삭제 실패: 인증되지 않은 사용자 (401 Unauthorized)")
    void deleteAssignment_fail_unauthenticated_it() throws Exception {
        // given
        var leader = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        var a = assignmentRepository.save(Assignment.builder()
                .study(study1).creator(leader)
                .title("unauth del").description("x")
                .startAt(LocalDateTime.now().minusDays(1).withNano(0))
                .dueAt(LocalDateTime.now().plusDays(3).withNano(0))
                .build());

        // when & then
        mockMvc.perform(delete("/api/studies/{studyId}/assignments/{assignmentId}", study1.getId(), a.getId())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        Assertions.assertThat(assignmentRepository.findById(a.getId())).isPresent();
    }
}
