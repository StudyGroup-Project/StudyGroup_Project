package com.study.focus.announcement;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.domain.Comment;
import com.study.focus.announcement.dto.AnnouncementUpdateDto;
import com.study.focus.announcement.dto.CreateCommentRequest;
import com.study.focus.announcement.dto.GetAnnouncementDetailResponse;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.notification.service.NotificationService;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementUnitTest {
    @Mock
    private AnnouncementRepository announcementRepo;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private GroupService groupService;

    @InjectMocks
    private AnnouncementService announcementService;


    @Mock
    private CommentRepository commentRepository;

    @Mock
    private S3Uploader s3uploader;


    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;


    @BeforeEach
    void setUp() {
        groupService = new GroupService(studyMemberRepository);
        announcementService = new AnnouncementService(
                announcementRepo,fileRepository
                ,s3uploader,commentRepository,userService,groupService,notificationService
        );
    }



    User testUser = User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build();
    Study testStudy = Study.builder().maxMemberCount(30).build();
    StudyMember teststudyMember = StudyMember.builder().user(testUser).study(testStudy).build();


    @Test
    @DisplayName("성공: 스터디 멤버가 공지사항 목록을 성공적으로 조회")
    void findAllSummaries_Success() {
        // given
        Long studyId = 1L;
        Long userId = 100L;

        Announcement a1 = Announcement.builder().id(1L).title("list1").build();
        Announcement a2 = Announcement.builder().id(2L).title("list2").build();
        List<Announcement> announcements = List.of(a1, a2);
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.ofNullable(teststudyMember));
        given(announcementRepo.findAllByStudyId(studyId)).willReturn(announcements);

        // when
        List<GetAnnouncementsResponse> result = announcementService.findAllSummaries(studyId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    @DisplayName(" 성공: 공지사항이 없을 경우 빈 리스트를 반환")
    void findAllSummaries_Success_EmptyList() {
        // given
        Long studyId = 1L;
        Long userId = 100L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.ofNullable(teststudyMember));
        given(announcementRepo.findAllByStudyId(studyId)).willReturn(List.of()); // 빈 리스트 반환 설정
        // when
        List<GetAnnouncementsResponse> result = announcementService.findAllSummaries(studyId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName(" 실패: 스터디 멤버가 아닐 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NotStudyMember() {
        // given
        Long studyId = 1L;
        Long userId = 999L;
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.findAllSummaries(studyId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("studyId가 null일 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NullStudyId() {
        Long userId = 100L;

        assertThatThrownBy(() -> announcementService.findAllSummaries(null, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName(" userId가 null일 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NullUserId() {
        // given
        Long studyId = 1L;

        // when & then
        assertThatThrownBy(() -> announcementService.findAllSummaries(studyId, null))
                .isInstanceOf(BusinessException.class);
    }


    @Test
    @DisplayName("공지사항 생성 성공 - 첨부 파일 포함")
    void createAnnouncement_success_WithFile(){

        Long studyId = 1L;
        Long userId = 1L;

        List<MultipartFile> mockFiles = List.of(
                new MockMultipartFile("files","test.jpg","image/jpg","test1".getBytes())
        );
        Study mockStudy = Study.builder().build();
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).role(StudyRole.LEADER).build();
        FileDetailDto mockFileDetail = new FileDetailDto("test.jpg", "testKey", "image/jpg", 10L);

        DataSize maxByte = DataSize.ofMegabytes(100);
        String bucketName = "test-bucket";

        // @Value 필드 값 주입
        ReflectionTestUtils.setField(s3uploader, "requestMaxByte", maxByte);
        ReflectionTestUtils.setField(s3uploader, "maxSizeByte", maxByte);
        ReflectionTestUtils.setField(s3uploader, "bucket", bucketName);
        Announcement announcement= Announcement.builder().id(1L).title("title").study(mockStudy).author(mockStudyMember).build();
        given(announcementRepo.save(any(Announcement.class))).willReturn(announcement);
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(mockStudyMember));
        doReturn(mockFileDetail).when(s3uploader).makeMetaData(any(MultipartFile.class));

        // when (실제 메서드 호출)
        announcementService.createAnnouncement(studyId, userId, "test title", "test content", mockFiles);


        then(announcementRepo).should(times(1)).save(any(Announcement.class));
        then(s3uploader).should(times(mockFiles.size())).makeMetaData(any(MultipartFile.class));
        then(fileRepository).should(times(mockFiles.size())).save(any(File.class));
        then(s3uploader).should(times(1)).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("공지사항 생성 - 첨부 파일이 없는 경우")
    void createAnnouncement_success_withoutFiles(){
        Long studyId = 1L;
        Long userId = 1L;

        Study mockStudy = Study.builder().build();
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).role(StudyRole.LEADER).build();
        Announcement announcement= Announcement.builder().id(1L).title("title").study(mockStudy).author(mockStudyMember).build();
        given(announcementRepo.save(any(Announcement.class))).willReturn(announcement);
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(mockStudyMember));

        // when
        announcementService.createAnnouncement(studyId, userId, "test title", "test content", null);
        //then
        then(announcementRepo).should(times(1)).save(any(Announcement.class));
        then(fileRepository).should(never()).save(any(File.class));
    }

    @Test
    @DisplayName("공지사항 생성 실패 - 파일 타입이 맞지 않는 경우 ")
    void createAnnouncement_fail_InvalidFileType()
    {
        Long studyId = 1L;
        Long userId = 1L;

        List<MultipartFile> invalidFiles = List.of(
                new MockMultipartFile("files", "test.exe", "application/octet-stream", "test".getBytes())
        );
        Study mockStudy = Study.builder().build();
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(mockStudyMember));

        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3uploader).makeMetaData(any(MultipartFile.class));


        BusinessException ex = assertThrows(BusinessException.class, () -> announcementService.createAnnouncement(studyId, userId, "title", "content", invalidFiles));

        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.INVALID_FILE_TYPE);
        then(announcementRepo).should(times(1)).save(any(Announcement.class));
        then(fileRepository).should(times(0)).save(any(File.class));

    }

    @Test
    @DisplayName("공지사항 생성 실패 - 방장이 아닌 경우  ")
    void createAnnouncement_fail_isNotLeader()
    {
        Long studyId = 1L;
        Long userId = 1L;

        Study mockStudy = Study.builder().build();
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).role(StudyRole.MEMBER).build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(mockStudyMember));

        BusinessException ex = assertThrows(BusinessException.class, () -> announcementService.createAnnouncement(studyId, userId, "title", "content", null));

        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.URL_FORBIDDEN);
        then(announcementRepo).should(times(0)).save(any(Announcement.class));
        then(fileRepository).should(times(0)).save(any(File.class));

    }

    @Test
    @DisplayName("공지사항 삭제 성공 - 방장 권한 존재, 파일/댓글 포함")
    void deleteAnnouncement_success(){
        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 1L;

        Study study = Study.builder().build();
        StudyMember leader = StudyMember.builder().role(StudyRole.LEADER).user(testUser).study(study).id(1L).build();
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .study(study)
                .author(leader)
                .title("title")
                .description("desc")
                .build();
        List<File> mockFiles = List.of(File.ofAnnouncement(announcement,
                new FileDetailDto("t","t","t",1L)));
        List<Comment> mockComments = List.of(Comment.builder().commenter(teststudyMember).content("test").build());

        given(studyMemberRepository.findByStudyIdAndUserId(studyId,userId)).willReturn(Optional.of(leader));
        given(announcementRepo.findByIdAndStudy_IdAndAuthor_Id(announcementId,studyId,userId)).willReturn(Optional.of(announcement));
        given(announcementRepo.findByIdAndStudy_IdAndAuthor_Id(studyId,userId,announcementId)).willReturn(Optional.of(announcement));
        given(fileRepository.findAllByAnnouncement_Id(announcementId)).willReturn(mockFiles);
        given(commentRepository.findAllByAnnouncement_Id(announcementId)).willReturn(mockComments);

        //then
        announcementService.deleteAnnouncement(studyId,userId,announcementId);

        then(fileRepository).should(times(1)).findAllByAnnouncement_Id(announcementId);
        then(commentRepository).should(times(1)).findAllByAnnouncement_Id(announcementId);
        then(commentRepository).should(times(1)).deleteAll(mockComments);
        then(announcementRepo).should(times(1)).delete(announcement);
    }

    @Test
    @DisplayName("공지사항 삭제 실패 - 방장이 아닌 경우")
    void deleteAnnouncement_Fail_isNotLeader(){
        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 1L;
        Study study = Study.builder().build();
        StudyMember leader = StudyMember.builder().role(StudyRole.MEMBER).user(testUser).build();
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .study(study)
                .author(leader)
                .title("title")
                .description("desc")
                .build();
       // given(announcementRepo.findByIdAndStudy_IdAndAuthor_Id(announcementId,studyId,announcementId)).willReturn(Optional.of(announcement));

        //then
        assertThatThrownBy(() ->announcementService.deleteAnnouncement(studyId,userId,announcementId))
                .isInstanceOf(BusinessException.class);

        then(commentRepository).should(never()).deleteAll(anyList());
        then(announcementRepo).should(never()).delete(any());
    }

    @Test
    @DisplayName("공지사항 삭제(존재하지 않는 공지)")
    void deleteAnnouncement_Fail_not_Found(){
        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 1L;

        Study study = Study.builder().build();
        StudyMember leader = StudyMember.builder().role(StudyRole.LEADER).user(testUser).build();
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .study(study)
                .author(leader)
                .title("title")
                .description("desc")
                .build();
        //when & then
        assertThatThrownBy(()-> announcementService.deleteAnnouncement(studyId,userId,announcementId))
                .isInstanceOf(BusinessException.class);
        then(commentRepository).should(never()).deleteAll(anyList());
        then(announcementRepo).should(never()).delete(any());
    }

    @Test
    @DisplayName("공지사항 수정 성공 - 제목/내용 업데이트 , 기존 파일 삭제, 새 파일 업로드")
    void updateAnnouncement_success() {
        // given
        Long studyId = 1L, userId = 1L, announcementId = 10L;
        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().id(studyId).study(study).role(StudyRole.LEADER).build();
        Announcement oldAnnouncement = Announcement.builder().id(announcementId).study(study).author(leader).
                title("old-title").
                description("old-content")
                .build();

        List<Long> deleteIds = List.of(100L, 200L);
        List<File> mockFiles = List.of(
                File.ofAnnouncement(oldAnnouncement, new FileDetailDto("test.png","test-key","image/png",10L))
        );
        MockMultipartFile newFile = new MockMultipartFile("files", "b.png", "image/png", "b".getBytes());

        // stub
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
        given(announcementRepo.findByIdAndStudy_IdAndAuthor_Id(announcementId, studyId, userId))
                .willReturn(Optional.of(oldAnnouncement));
        given(fileRepository.findAllById(deleteIds)).willReturn(mockFiles);
        given(s3uploader.makeMetaData(any(MultipartFile.class)))
                .willReturn(new FileDetailDto("b.png", "key-b", "image/png", 5L));

        AnnouncementUpdateDto updateDto = new AnnouncementUpdateDto("new-title", "new-content", List.of(newFile), deleteIds);

        // when
        announcementService.updateAnnouncement(studyId, announcementId, userId, updateDto);

        // then
        then(fileRepository).should(times(1)).findAllById(deleteIds);
        then(fileRepository).should(times(1)).saveAll(mockFiles);
        then(s3uploader).should(times(1)).makeMetaData(any(MultipartFile.class));
        then(s3uploader).should(times(1)).uploadFiles(anyList(), anyList());
    }

    @Test
    @DisplayName("공지사항 수정 실패 - 공지가 존재하지 않음")
    void updateAnnouncement_fail_notFound() {
        Long studyId = 1L, userId = 1L, announcementId = 10L;
        Study study = Study.builder().id(studyId).build();
        StudyMember leader = StudyMember.builder().id(userId).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(leader));
        given(announcementRepo.findByIdAndStudy_IdAndAuthor_Id(announcementId, studyId, userId))
                .willReturn(Optional.empty());


        assertThatThrownBy(() ->
                announcementService.updateAnnouncement(studyId, announcementId, userId, null))
                .isInstanceOf(BusinessException.class);
    }



    @Test
    @DisplayName("공지 상세 데이터 가져오기 성공 - 댓글/파일 포함")
    void getAnnouncementDetail_success() {
        // given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        StudyMember member = StudyMember.builder().id(userId).user(testUser).study(testStudy).role(StudyRole.MEMBER).build();
        Announcement announcement = Announcement.builder().id(announcementId).study(testStudy).
                author(member).
                title("공지 제목").
                description("공지 내용").build();
        Comment comment = Comment.builder().id(100L).commenter(member).content("댓글 내용").
                announcement(announcement).build();

        File file = File.ofAnnouncement(announcement,
                new FileDetailDto("test.jpg", "testKey", "image/jpg", 10L));
        ReflectionTestUtils.setField(file, "id", 200L);

        GetMyProfileResponse mockProfile = new GetMyProfileResponse(1L,
                "testName","testProvince","testDistrict","testBirth",
                Job.JOB_SEEKER, Category.ACADEMICS,"tesUrl",30L);

        // mocking
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(member));
        given(announcementRepo.findByIdAndStudyId(announcementId, studyId))
                .willReturn(Optional.of(announcement));
        given(userService.getMyProfile(any()))
                .willReturn(mockProfile);
        given(commentRepository.findAllByAnnouncement_Id(announcementId))
                .willReturn(List.of(comment));
        given(fileRepository.findAllByAnnouncement_Id(announcementId))
                .willReturn(List.of(file));
        given(s3uploader.getUrlFile(file.getFileKey()))
                .willReturn("https://s3/testKey");

        // when
        GetAnnouncementDetailResponse result =
                announcementService.getAnnouncementDetail(studyId, announcementId, userId);


        assertThatThrownBy(() ->
                announcementService.updateAnnouncement(studyId, announcementId, userId, null))
                .isInstanceOf(BusinessException.class);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getAnnouncementId()).isEqualTo(announcementId);
        assertThat(result.getTitle()).isEqualTo("공지 제목");
        assertThat(result.getContent()).isEqualTo("공지 내용");
        assertThat(result.getUserName()).isEqualTo("testName");
        assertThat(result.getUserProfileImageUrl()).isEqualTo("tesUrl");
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getFiles()).hasSize(1);
        then(userService).should(atLeastOnce()).getMyProfile(any());
        then(s3uploader).should(times(1)).getUrlFile(file.getFileKey());

    }



    @Test
    @DisplayName("공지 상세 데이터 가져오기 실패 - 스터디 멤버 아닌 경우")
    void getAnnouncementDetail_fail_NotStudyMember() {
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.getAnnouncementDetail(studyId, announcementId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("공지 상세 데이터 가져오기 실패 - 공지가 존재하지 않음")
    void getAnnouncementDetail_fail_AnnouncementNotFound() {
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(announcementRepo.findByIdAndStudyId(announcementId, studyId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.getAnnouncementDetail(studyId, announcementId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("공지에 댓글 달기 성공")
    void addComment_success(){

        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        StudyMember member = StudyMember.builder().id(userId).user(testUser).study(testStudy).role(StudyRole.MEMBER).build();
        Announcement announcement = Announcement.builder().id(announcementId).study(testStudy).
                author(member).
                title("공지 제목").
                description("공지 내용").build();
        Comment comment = Comment.builder().id(100L).commenter(member).content("댓글 내용").
                announcement(announcement).build();


        given(studyMemberRepository.findByStudyIdAndUserId(studyId,userId))
                .willReturn(Optional.of(member));
        given(announcementRepo.findByIdAndStudyId(announcementId,studyId))
                .willReturn(Optional.of(announcement));

        given(commentRepository.save(any())).willReturn(comment);


        //when
        announcementService.addComment(studyId,announcementId,userId,
                CreateCommentRequest.builder().content("test").build());


        //then
        then(studyMemberRepository).should(times(1)).findByStudyIdAndUserId(studyId,userId);
        then(announcementRepo).should(times(1)).findByIdAndStudyId(announcementId,studyId) ;
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("공지에 댓글 달기 실패 - 스터디 멤버가 아닌 경우")
    void addComment_Fail_isNotMember(){

        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId,userId))
                .willReturn(Optional.empty());
        //when
        Assertions.assertThatThrownBy(() -> {
            announcementService.addComment(studyId,announcementId,userId,
                    CreateCommentRequest.builder().content("test").build());
        }).isInstanceOf(BusinessException.class);


        //then
        then(studyMemberRepository).should(times(1)).findByStudyIdAndUserId(studyId,userId);
        then(announcementRepo).should(times(0)).findByIdAndStudyId(announcementId,studyId) ;
        then(commentRepository).should(times(0)).save(any(Comment.class));
    }

    @Test
    @DisplayName("공지에 댓글 달기 실패 : 공지가 없는 경우")
    void addComment_Fail_AnnouncementNotFound(){

        //given
        Long studyId = 1L;
        Long userId = 1L;
        Long announcementId = 10L;

        StudyMember member = StudyMember.builder().id(userId).user(testUser).study(testStudy).role(StudyRole.MEMBER).build();
        Announcement announcement = Announcement.builder().id(announcementId).study(testStudy).
                author(member).
                title("공지 제목").
                description("공지 내용").build();
        Comment comment = Comment.builder().id(100L).commenter(member).content("댓글 내용").
                announcement(announcement).build();


        given(studyMemberRepository.findByStudyIdAndUserId(studyId,userId))
                .willReturn(Optional.of(member));
        given(announcementRepo.findByIdAndStudyId(announcementId,studyId))
                .willReturn(Optional.empty());


        //when
        Assertions.assertThatThrownBy(() -> {
            announcementService.addComment(studyId,announcementId,userId,
                    CreateCommentRequest.builder().content("test").build());
        }).isInstanceOf(BusinessException.class);



        //then
        then(studyMemberRepository).should(times(1)).findByStudyIdAndUserId(studyId,userId);
        then(announcementRepo).should(times(1)).findByIdAndStudyId(announcementId,studyId) ;
        then(commentRepository).should(times(0)).save(any(Comment.class));
    }


}