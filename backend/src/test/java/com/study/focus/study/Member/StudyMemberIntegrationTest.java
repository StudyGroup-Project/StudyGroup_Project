package com.study.focus.study.Member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.GetStudyMembersResponse;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StudyMemberIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // (Repositories and other fields are the same)
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private FileRepository fileRepository;

    @MockitoBean
    private S3Uploader s3Uploader;

    private User testUser;
    private User leader;
    private User member; // 추방될 멤버
    private UserProfile leaderProfile;
    private UserProfile memberProfile;
    private Study testStudy;
    private StudyProfile testProfile;

    @BeforeEach
    void setUp() {
        // 기존 setUp 코드에 '추방될 멤버'를 추가
        testUser = userRepository.save(User.builder().build());
        leader = userRepository.save(User.builder().trustScore(82).build());
        member = userRepository.save(User.builder().build()); // <-- 추방 대상 멤버 생성

        testStudy = studyRepository.save(Study.builder()
                .recruitStatus(RecruitStatus.OPEN)
                .maxMemberCount(10)
                .build());
        testProfile = studyProfileRepository.save(StudyProfile.builder()
                .study(testStudy)
                .title("알고리즘 스터디")
                .bio("매주 알고리즘 문제 풀이")
                .description("알고리즘 문제를 풀고 토론하는 스터디입니다.")
                .category(Category.IT)
                .address(Address.builder().province("경상북도").district("경산시").build())
                .build());

        // 방장 멤버 추가
        studyMemberRepository.save(StudyMember.builder()
                .study(testStudy)
                .user(leader)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());

        // 추방될 일반 멤버 추가
        studyMemberRepository.save(StudyMember.builder()
                .study(testStudy)
                .user(member) // <-- 추방될 멤버를 스터디에 추가
                .role(StudyRole.MEMBER)
                .status(StudyMemberStatus.JOINED)
                .build());

         userProfileRepository.save(UserProfile
                .builder().user(leader).nickname("leader")
                .birthDate(LocalDate.now()).job(Job.FREELANCER)
                         .address(Address.builder().district("dis").province("pro").build())
                .preferredCategory(Category.IT).build());

        userProfileRepository.save(UserProfile
                .builder().user(member).nickname("member")
                .address(Address.builder().district("dis").province("pro").build())
                .birthDate(LocalDate.now()).job(Job.FREELANCER)
                .preferredCategory(Category.IT).build());
    }

    @AfterEach
    void tearDown() {
        // (tearDown is the same)
        applicationRepository.deleteAll();
        bookmarkRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("스터디 멤버 목록 조회 - 멤버가 있는 경우")
    void getMembers_Success() throws Exception {

        // when
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/" + testStudy.getId() + "/members")
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        GetStudyMembersResponse result = objectMapper.readValue(response, GetStudyMembersResponse.class);

        assertThat(result.getStudyId()).isEqualTo(testStudy.getId());
        assertThat(result.getMembers()).hasSize(2);
    }


    @Test
    @DisplayName("스터디 멤버 목록 조회 실패 - 스터디 멤버가 아닌 사용자")
    void getMembers_Fail_NotStudyMember() throws Exception {
        // given
        User outsider = userRepository.save(User.builder().build());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/" + testStudy.getId() + "/members")
                .with(user(new CustomUserDetails(outsider.getId())))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("그룹 인원 추방 - 성공")
    void expelMember_Success() throws Exception {
        // given
        long initialMemberCount = studyMemberRepository.count();

        // when
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), member.getId())
                        .with(user(new CustomUserDetails(leader.getId()))).with(csrf()))
                .andExpect(status().isNoContent());

        // then
        StudyMember expelledMember = studyMemberRepository.findByStudyIdAndUserId(testStudy.getId(), member.getId())
                .orElseThrow(() -> new AssertionError("멤버가 삭제되면 안됩니다."));

        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount);
        assertThat(expelledMember.getStatus()).isEqualTo(StudyMemberStatus.BANNED);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 방장이 아닌 경우")
    void expelMember_Fail_NotLeader() throws Exception {
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), leader.getId())
                        .with(user(new CustomUserDetails(member.getId()))).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 자기 자신을 추방하는 경우")
    void expelMember_Fail_SelfExpulsion() throws Exception {
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), leader.getId())
                        .with(user(new CustomUserDetails(leader.getId()))).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 대상이 그룹 멤버가 아닌 경우")
    void expelMember_Fail_TargetNotMember() throws Exception {
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), testUser.getId())
                        .with(user(new CustomUserDetails(leader.getId()))).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // --- 그룹 탈퇴 (leaveStudy) 테스트 ---

    @Test
    @DisplayName("그룹 탈퇴 - 성공")
    void leaveStudy_Success() throws Exception {
        // given
        long initialMemberCount = studyMemberRepository.count();

        // when
        mockMvc.perform(delete("/api/studies/{studyId}/members/me", testStudy.getId())
                        .with(user(new CustomUserDetails(member.getId()))).with(csrf()))
                .andExpect(status().isNoContent());

        // then
        StudyMember leftMember = studyMemberRepository.findByStudyIdAndUserId(testStudy.getId(), member.getId())
                .orElseThrow(() -> new AssertionError("멤버가 삭제되면 안됩니다."));

        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount);
        assertThat(leftMember.getStatus()).isEqualTo(StudyMemberStatus.LEFT);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 방장은 탈퇴할 수 없음")
    void leaveStudy_Fail_LeaderCannotLeave() throws Exception {
        mockMvc.perform(delete("/api/studies/{studyId}/members/me", testStudy.getId())
                        .with(user(new CustomUserDetails(leader.getId()))).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 멤버가 아닌 사용자의 요청")
    void leaveStudy_Fail_NotAMember() throws Exception {
        mockMvc.perform(delete("/api/studies/{studyId}/members/me", testStudy.getId())
                        .with(user(new CustomUserDetails(testUser.getId()))).with(csrf()))
                .andExpect(status().isBadRequest());
    }


}
