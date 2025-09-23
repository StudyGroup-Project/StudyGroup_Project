package com.study.focus.announcement;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.domain.File;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest

@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private AnnouncementService announcementService;

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
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());
        StudyMember studyMember2 = studyMemberRepository.save(StudyMember.builder().user(user1).study(study2).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED).build());
        StudyMember studyMember3 = studyMemberRepository.save(StudyMember.builder().user(user2).study(study2).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());


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

    @Test
    @DisplayName("성공: 공지사항 생성 - 첨부 파일 포함 ")
    void createAnnouncement_withFile_success() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "testFile","hello.txt","text/plain","Test".getBytes());
        String title = "testTile";
        String content = "testContent";

        //when and then
        List<Announcement> beforeSaveList = announcementRepository.findAllByStudyId(study1.getId());
        mockMvc.perform(multipart("/api/studies/" +study1.getId()+"/announcements")
                .file(file)
                .param("title",title)
                .param("content",content)
                .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isOk());

        List<Announcement> afterSaveList = announcementRepository.findAllByStudyId(study1.getId());
        Assertions.assertThat(beforeSaveList.size() +1).isEqualTo(afterSaveList.size());
    }

    @Test
    @DisplayName("성공: 공지사항 생성 - 첨부 파일 제외 ")
    void createAnnouncement_withoutFile_success() throws Exception {
        //given
        String title = "testTile";
        String content = "testContent";
        List<Announcement> beforeSaveList = announcementRepository.findAllByStudyId(study1.getId());
        List<File> files = fileRepository.findAll();

        //when and then
        mockMvc.perform(multipart("/api/studies/" +study1.getId()+"/announcements")
                        .param("title",title)
                        .param("content",content)
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isOk());
        List<Announcement> afterSaveList = announcementRepository.findAllByStudyId(study1.getId());
        Assertions.assertThat(beforeSaveList.size() + 1).isEqualTo(afterSaveList.size());
        Assertions.assertThat(files.size()).isEqualTo(0);
    }




    @Test
    @DisplayName("실패: 공지사항 생성 - 지원하지 않는 파일 타입 ")
    void createAnnouncement_fail_invalidFileType() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.exe",
                "application/octet-stream",
                "test".getBytes()
        );
        String title = "testTile";
        String content = "testContent";

        long initialCount = announcementRepository.count();
        mockMvc.perform(multipart("/api/studies/" +study1.getId()+"/announcements")
                        .file(file)
                        .param("title",title)
                        .param("content",content)
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                        .andExpect(status().isBadRequest());
        //rollback
        Assertions.assertThat(announcementRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("실패: 공지사항 생성 - 방장이 아닌 경우")
    void createAnnouncement_fail_isNotLeader() throws Exception {
        //given
        String title = "testTile";
        String content = "testContent";

        //when and then
        mockMvc.perform(multipart("/api/studies/" +study2.getId()+"/announcements")
                        .param("title",title)
                        .param("content",content)
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}

