package com.study.focus.assignment;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.repository.AssignmentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        assignmentRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String iso(LocalDateTime t) {
        return t.withNano(0).format(ISO); // ModelAttribute 바인딩 기본 ISO 형식
    }

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
}
