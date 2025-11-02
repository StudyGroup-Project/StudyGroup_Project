package com.study.focus.announcement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.domain.Comment;
import com.study.focus.announcement.dto.CreateCommentRequest;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.notification.repository.NotificationRepository;
import com.study.focus.study.domain.RecruitStatus;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
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

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

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
        notificationRepository.deleteAll();
        userProfileRepository.deleteAll();
        commentRepository.deleteAll();
        fileRepository.deleteAll();
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
                .andExpect(status().isCreated());

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
                .andExpect(status().isCreated());
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
        Assertions.assertThat(announcementRepository.count()).isEqualTo(initialCount +1);
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

    @Test
    @DisplayName("성공: 공지사항 삭제 / 방장 권한이 있고 파일과 댓글도 있는 경우 ")
    void deleteAnnouncement_success() throws Exception {
        StudyMember leadMember = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElse(null);
        Announcement announcement = announcementRepository.save(Announcement.builder()
                .author(leadMember)
                .study(study1)
                .title("title")
                .description("cotent")
                .build());

        Long announcementId = announcement.getId();
        fileRepository.save(
                File.ofAnnouncement(announcement,
                        new FileDetailDto("o.txt","key","txt",10L))
        );

        mockMvc.perform(delete("/api/studies/"+ study1.getId()+"/announcements/" +announcementId)
                .with(user(new CustomUserDetails(user1.getId())))
                .with(csrf())).andExpect(status().isOk());

    }

    @Test
    @DisplayName("실패: 공지사항 삭제 - 방장이 아닌 경우 ")
    void deleteAnnouncement_Fail_isNotLeader() throws Exception {
        long initialCount = announcementRepository.count();
        mockMvc.perform(delete("/api/studies/"+ study2.getId()+"/announcements/" +2L)
                .with(user(new CustomUserDetails(user1.getId())))
                .with(csrf())).andExpect(status().isForbidden());
        Assertions.assertThat(announcementRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("실패: 공지사항 삭제 - 스터디 멤버가 아닌 경우 ")
    void deleteAnnouncement_Fail_isNotStudyMember() throws Exception {
        long initialCount = announcementRepository.count();
        mockMvc.perform(delete("/api/studies/"+ study1.getId()+"/announcements/" +2L)
                .with(user(new CustomUserDetails(user2.getId())))
                .with(csrf())).andExpect(status().isBadRequest());
        Assertions.assertThat(announcementRepository.count()).isEqualTo(initialCount);

    }

    @Test
    @DisplayName("실패: 공지사항 삭제 - 존재하지 않는 공지인 경우")
    void deleteAnnouncement_Fail_announcementNotFound()throws Exception{
        long NotExistAnnouncementId = 99999L;
        long initialCount = announcementRepository.count();
        mockMvc.perform(delete("/api/studies/"+ study1.getId()+"/announcements/" +NotExistAnnouncementId)
                .with(user(new CustomUserDetails(user1.getId())))
                .with(csrf())).andExpect(status().isBadRequest());
        Assertions.assertThat(announcementRepository.count()).isEqualTo(initialCount);

    }

    @Test
    @DisplayName("성공: 공지사항 수정 - 제목/내용 변경 + 파일 삭제 및 추가")
    void updateAnnouncement_success() throws Exception {
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(study1.getId(), user1.getId()).orElseThrow();
        Announcement announcement = announcementRepository.save(Announcement.builder()
                .author(studyMember)
                .study(study1)
                .title("title")
                .description("cotent")
                .build());
        File fileSaved = fileRepository.save(File.ofAnnouncement(announcement,
                new FileDetailDto("o.txt", "key", "txt", 10L)));
        MockMultipartFile newFiles = new MockMultipartFile(
                "testFile","hello.txt","text/plain","Test".getBytes());

        mockMvc.perform(multipart("/api/studies/" + study1.getId() + "/announcements/" + announcement.getId())
                        .file(newFiles)
                        .param("title", "updated title")
                        .param("content", "updated content")
                        .param("deleteFileIds", fileSaved.getId().toString())
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("실패: 공지사항 수정 - 존재하지 않는 공지")
    void updateAnnouncement_fail_notFound() throws Exception {
        Long notExistId = 99999L;
        mockMvc.perform(multipart("/api/studies/"+ study1.getId() + "/announcements/" + notExistId)
                        .param("title", "new-title")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf())
                        .with(request -> { request.setMethod("PUT"); return request; })
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 공지사항 상세 조회 - 댓글/파일 포함")
    void getAnnouncementDetail_success() throws Exception
    {
        //given
        //유저 및 프로필 생성
        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());
        UserProfile userProfile = UserProfile.builder().user(user)
                .nickname("testNickname").address(Address.builder().province("testProvince").district("testDistrict").build())
                .birthDate(LocalDateTime.now().toLocalDate()).job(Job.FREELANCER).preferredCategory(List.of(Category.IT)).build();
        userProfileRepository.save(userProfile);

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());
        Announcement announcement = announcementRepository.save(Announcement.builder().study(study1).author( studyMember).title("title").description("content")
                .build());

        // 공지에대한 댓글 및 파일 생성
        commentRepository.save(Comment.builder()
                .announcement(announcement).commenter(studyMember).
                content("commentContent").build());
        fileRepository.save(File.ofAnnouncement(announcement,
                new FileDetailDto("o.txt","key","txt",10L)));

        //when and then
        mockMvc.perform(get("/api/studies/"+ study1.getId()+"/announcements/" + announcement.getId())
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 상세 데이터 가져오기 실패 - 스터디 멤버 아닌 경우")
    void getAnnouncementDetail_fail_NotStudyMember() throws Exception
    {
        //given
        //유저 및 프로필 생성
        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());
        Announcement announcement = announcementRepository.save(Announcement.builder().study(study1).author( studyMember).title("title").description("content")
                .build());


        //when and then
        mockMvc.perform(get("/api/studies/"+ 100L+"/announcements/" + announcement.getId())
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("공지 상세 데이터 가져오기 실패 - 공지가 존재하지 않음")
    void getAnnouncementDetail_fail_AnnouncementNotFound() throws Exception
    {
        //given
        //유저 및 프로필 생성
        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());

        //when and then
        mockMvc.perform(get("/api/studies/"+ study1.getId()+"/announcements/" + 100L)
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지에 댓글 달기 성공")
    void addComment_success() throws Exception {

        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());

        Announcement announcement = announcementRepository.
                save(Announcement.builder().study(study1).author(studyMember).title("TestTitle1").build());

        CreateCommentRequest reqeust = CreateCommentRequest.builder()
                .content("test").build();
        String requestJson = objectMapper.writeValueAsString(reqeust);


        mockMvc.perform(post("/api/studies/"+ study1.getId()+"/announcements/" +
                announcement.getId()+"/comments").
                with(user(new CustomUserDetails(user.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("공지에 댓글 달기 실패 : 공지가 없는 경우")
    void addComment_Fail_AnnouncementNotFound() throws Exception {

        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());

        Announcement announcement = announcementRepository.
                save(Announcement.builder().study(study1).author(studyMember).title("TestTitle1").build());

        CreateCommentRequest reqeust = CreateCommentRequest.builder()
                .content("test").build();
        String requestJson = objectMapper.writeValueAsString(reqeust);


        mockMvc.perform(post("/api/studies/"+ study1.getId()+"/announcements/" +
                100L+"/comments").
                with(user(new CustomUserDetails(user.getId())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지에 댓글 달기 실패 - 스터디 멤버가 아닌 경우")
    void addComment_Fail_isNotMember() throws Exception {

        User user = userRepository.save(User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build());

        //study Member 및 공지 생성
        StudyMember studyMember = studyMemberRepository.save(StudyMember.builder().user(user).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());

        Announcement announcement = announcementRepository.
                save(Announcement.builder().study(study1).author(studyMember).title("TestTitle1").build());

        CreateCommentRequest reqeust = CreateCommentRequest.builder()
                .content("test").build();
        String requestJson = objectMapper.writeValueAsString(reqeust);


        mockMvc.perform(post("/api/studies/"+ 100L+"/announcements/" +
                announcement.getId()+"/comments").
                with(user(new CustomUserDetails(user.getId())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)).andExpect(status().isBadRequest());
    }




}

