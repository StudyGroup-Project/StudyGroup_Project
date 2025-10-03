package com.study.focus.study.Member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("그룹 인원 추방 - 성공")
    void expelMember_Success() throws Exception {
        // given: setUp에서 방장(leader)과 추방될 멤버(member)가 이미 스터디에 속해 있음
        long initialMemberCount = studyMemberRepository.count();

        // when: 방장 권한으로 멤버 추방 API 호출
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), member.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isOk());

        // then: DB에서 멤버가 실제로 삭제되었는지 확인
        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount - 1);
        assertThat(studyMemberRepository.findByStudyIdAndUserId(testStudy.getId(), member.getId())).isNotPresent();
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 방장이 아닌 경우")
    void expelMember_Fail_NotLeader() throws Exception {
        // given: 방장이 아닌 일반 멤버(member)가 다른 멤버(leader)를 추방하려 시도
        long initialMemberCount = studyMemberRepository.count();

        // when: 일반 멤버 권한으로 추방 API 호출
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), leader.getId())
                        .with(user(new CustomUserDetails(member.getId()))) // 방장이 아닌 사용자로 인증
                        .with(csrf()))
                .andExpect(status().isForbidden()); // then: 권한 없음(403) 응답을 기대

        // then: 멤버 수가 변하지 않았는지 확인
        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 자기 자신을 추방하는 경우")
    void expelMember_Fail_SelfExpulsion() throws Exception {
        // given: 방장이 자기 자신을 추방 대상으로 지정
        long initialMemberCount = studyMemberRepository.count();

        // when: 자기 자신을 추방하는 API 호출
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), leader.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // then: 잘못된 요청(400) 응답을 기대

        // then: 멤버 수가 변하지 않았는지 확인
        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 대상이 그룹 멤버가 아닌 경우")
    void expelMember_Fail_TargetNotMember() throws Exception {
        // given: 스터디에 속하지 않은 'testUser'를 추방 대상으로 지정
        long initialMemberCount = studyMemberRepository.count();

        // when: 그룹 멤버가 아닌 유저를 추방하는 API 호출
        mockMvc.perform(delete("/api/studies/{studyId}/members/{userId}", testStudy.getId(), testUser.getId())
                        .with(user(new CustomUserDetails(leader.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // then: 잘못된 파라미터(400) 응답을 기대

        // then: 멤버 수가 변하지 않았는지 확인
        assertThat(studyMemberRepository.count()).isEqualTo(initialMemberCount);
    }


}
