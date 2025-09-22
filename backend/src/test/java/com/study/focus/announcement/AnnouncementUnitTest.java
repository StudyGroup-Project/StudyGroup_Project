package com.study.focus.announcement;

import com.study.focus.account.domain.User;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyMemberRepository;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.Assertions;
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
    private AnnouncementService announcementService;


    @Mock
    private S3Uploader s3uploader;




    User testUser = User.builder().trustScore(30L).lastLoginAt(LocalDateTime.now()).build();
    Study testStudy = Study.builder().maxMemberCount(30).build();
    StudyMember teststudyMember = StudyMember.builder().user(testUser).study(testStudy).build();
    Announcement testAnnouncement = Announcement.builder().study(testStudy).author(teststudyMember)
            .title("test").description("test").build();

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
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).build();
        FileDetailDto mockFileDetail = new FileDetailDto("test.jpg", "testKey", "image/jpg", 10L);

        DataSize maxByte = DataSize.ofMegabytes(100);
        String bucketName = "test-bucket";

        // @Value 필드 값 주입
        ReflectionTestUtils.setField(s3uploader, "requestMaxByte", maxByte);
        ReflectionTestUtils.setField(s3uploader, "maxSizeByte", maxByte);
        ReflectionTestUtils.setField(s3uploader, "bucket", bucketName);

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
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).build();
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
        StudyMember mockStudyMember = StudyMember.builder().study(mockStudy).build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).willReturn(Optional.of(mockStudyMember));

        doThrow(new BusinessException(UserErrorCode.INVALID_FILE_TYPE))
                .when(s3uploader).makeMetaData(any(MultipartFile.class));


        BusinessException ex = assertThrows(BusinessException.class, () -> announcementService.createAnnouncement(studyId, userId, "title", "content", invalidFiles));

        org.assertj.core.api.Assertions.assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.INVALID_FILE_TYPE);
        then(announcementRepo).should(times(1)).save(any(Announcement.class));
        then(fileRepository).should(times(0)).save(any(File.class));

    }

}