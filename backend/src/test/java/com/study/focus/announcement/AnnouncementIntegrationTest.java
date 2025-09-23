package com.study.focus.announcement;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AnnouncementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;


    private Study study1;
    private Study study2;
    private User user1;
    private  User user2;

    //임시 데이터 삽입
    @BeforeEach
    void setUp() {

        user1 = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());
        user2 = userRepository.save(User.builder().trustScore(100L).lastLoginAt(LocalDateTime.now()).build());
        study1 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());
        study2 = studyRepository.save(Study.builder().maxMemberCount(30).recruitStatus(RecruitStatus.OPEN).build());


        StudyMember studyMember1 = studyMemberRepository.save(StudyMember.builder().user(user1).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED).build());
        StudyMember studyMember2 = studyMemberRepository.save(StudyMember.builder().user(user1).study(study2).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED).build());
        StudyMember studyMember3 = studyMemberRepository.save(StudyMember.builder().user(user2).study(study2).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED).build());


        announcementRepository.save(Announcement.builder().study(study1).author(studyMember1).title("TestTitle1").build());
        announcementRepository.save(Announcement.builder().study(study1).author(studyMember1).title("TestTitle2").build());
    }

    @AfterEach
    void after() {
        announcementRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 스터디 멤버가 공지사항 목록을 성공적으로 조회")
    void getAnnouncements_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/studies/" + study1.getId() + "/announcements")
                        .with(user(new CustomUserDetails(user1.getId())))) // setUp에서 생성된 user를 사용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("TestTitle1"));
    }


    @Test
    @DisplayName("실패: 스터디 멤버가 아닐 경우 IllegalArgumentException이 발생")
    void getAnnouncements_Fail_NotStudyMember() throws Exception {
        mockMvc.perform(get("/api/studies/" + study1.getId() + "/announcements")
                        .with(user(new CustomUserDetails(user2.getId()))))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("성공: 빈 멤버 리스트 반환")
    void getAnnouncements_success_IsEmptyList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/studies/" + study2.getId() + "/announcements")
                        .with(user(new CustomUserDetails(user1.getId())))) // setUp에서 생성된 user를 사용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }
}

