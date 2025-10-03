package com.study.focus.assignment;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmissionIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private FileRepository fileRepository;

    @MockBean private S3Uploader s3Uploader; // 외부 I/O 차단

    private User user1;     // 제출자
    private User user2;     // 미소속/권한 없음 시나리오용
    private Study study;
    private StudyMember memberUser1; // user1의 study 멤버십
    private Assignment openAssignment; // 마감 전
    private Assignment closedAssignment; // 마감 지난 과제

    @BeforeEach
    void setUp() {
        // 사용자
        user1 = userRepository.save(User.builder().trustScore(10L).lastLoginAt(LocalDateTime.now()).build());
        user2 = userRepository.save(User.builder().trustScore(20L).lastLoginAt(LocalDateTime.now()).build());

        // 스터디 & 멤버십
        study = studyRepository.save(Study.builder().recruitStatus(RecruitStatus.OPEN).maxMemberCount(30).build());
        memberUser1 = studyMemberRepository.save(StudyMember.builder()
                .user(user1).study(study)
                .role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());

        // 과제들
        LocalDateTime now = LocalDateTime.now().withNano(0);
        openAssignment = assignmentRepository.save(Assignment.builder()
                .study(study).creator(memberUser1)
                .title("open").description("d")
                .startAt(now.minusDays(1))
                .dueAt(now.plusDays(3)) // 마감 전
                .build());

        closedAssignment = assignmentRepository.save(Assignment.builder()
                .study(study).creator(memberUser1)
                .title("closed").description("d")
                .startAt(now.minusDays(5))
                .dueAt(now.minusDays(1)) // 마감 지남
                .build());
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


    @Test
    @DisplayName("성공: 파일 없이 제출 → 201 Created + Location 헤더 + DB 1건 증가")
    void submit_success_withoutFiles() throws Exception {
        long beforeSubmissions = submissionRepository.count();
        long beforeFiles = fileRepository.count();

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .param("description", "my first submit")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/studies/" + study.getId()
                                + "/assignments/" + openAssignment.getId() + "/submissions/")));

        Assertions.assertThat(submissionRepository.count()).isEqualTo(beforeSubmissions + 1);
        Assertions.assertThat(fileRepository.count()).isEqualTo(beforeFiles);
        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("성공: 파일과 함께 제출 → 201 Created + 업로드 호출")
    void submit_success_withFiles() throws Exception {
        long beforeSubmissions = submissionRepository.count();
        long beforeFiles = fileRepository.count();

        // S3 메타/업로드 목
        when(s3Uploader.makeMetaData(any())).thenAnswer(inv -> {
            var f = (org.springframework.mock.web.MockMultipartFile) inv.getArgument(0);
            return new FileDetailDto(f.getOriginalFilename(), "key-" + f.getOriginalFilename(),
                    f.getContentType(), f.getSize());
        });
        doNothing().when(s3Uploader).uploadFiles(anyList(), anyList());

        var f1 = new MockMultipartFile("files", "a.txt", MediaType.TEXT_PLAIN_VALUE, "A".getBytes());
        var f2 = new MockMultipartFile("files", "b.pdf", MediaType.APPLICATION_PDF_VALUE, "B".getBytes());

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .file(f1)
                        .file(f2)
                        .param("description", "with files")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/studies/" + study.getId()
                                + "/assignments/" + openAssignment.getId() + "/submissions/")));

        Assertions.assertThat(submissionRepository.count()).isEqualTo(beforeSubmissions + 1);
        // 구현이 soft-insert/연결 저장 방식에 따라 다르지만 통상 2개 증가
        Assertions.assertThat(fileRepository.count()).isEqualTo(beforeFiles + 2);

        verify(s3Uploader, times(2)).makeMetaData(any());
        verify(s3Uploader, times(1)).uploadFiles(anyList(), anyList());
    }

    // ========= 실패 케이스 =========

    @Test
    @DisplayName("실패: 마감 이후 제출 금지 → 400 Bad Request")
    void submit_fail_afterDeadline() throws Exception {
        long before = submissionRepository.count();

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), closedAssignment.getId())
                        .param("description", "too late")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(submissionRepository.count()).isEqualTo(before);
        verify(s3Uploader, never()).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("실패: 같은 유저의 중복 제출 → 400 Bad Request")
    void submit_fail_duplicateSameUser() throws Exception {
        // 최초 1회 성공 저장(직접 리포지토리로 심기)
        submissionRepository.save(com.study.focus.assignment.domain.Submission.builder()
                .assignment(openAssignment).submitter(memberUser1).description("first").build());

        long before = submissionRepository.count();

        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .param("description", "second")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(submissionRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("실패: 스터디 멤버가 아님 → 400 Bad Request")
    void submit_fail_notStudyMember() throws Exception {
        long before = submissionRepository.count();

        // user2는 해당 study 미소속
        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .param("description", "no member")
                        .with(user(new CustomUserDetails(user2.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertThat(submissionRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("실패: 인증되지 않은 사용자 → 401 Unauthorized")
    void submit_fail_unauthenticated() throws Exception {
        mockMvc.perform(multipart("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .param("description", "no auth")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    // (옵션) 잘못된 HTTP 메서드/컨텐츠 타입 등
    @Test
    @DisplayName("실패(옵션): multipart 아님 → 415 또는 400 (컨트롤러 설정에 따라)")
    void submit_fail_wrongContentType() throws Exception {
        mockMvc.perform(post("/api/studies/{studyId}/assignments/{assignmentId}/submissions",
                        study.getId(), openAssignment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"json\"}")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
