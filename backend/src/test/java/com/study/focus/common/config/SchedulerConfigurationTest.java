package com.study.focus.common.config;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.config.S3TestConfig;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import(S3TestConfig.class)
class FileCleanupSchedulerTest {

    @Autowired
    private FileCleanupScheduler scheduler;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Uploader s3Uploader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    AnnouncementRepository announcementRepository;


    private File fileToDelete1;
    private File fileToDelete2;
    private File fileToKeep;
    private User user1;
    private Study study1;

    @BeforeEach
    void setUp(){
        //기초 데이터 준비 - 유저, 스터디, 스터디멤버, 공지사항
        user1 = userRepository.save(User.builder().trustScore(30L).
                lastLoginAt(LocalDateTime.now()).build());

        study1 = studyRepository.save(Study.builder().maxMemberCount(30).
                recruitStatus(RecruitStatus.OPEN).build());

        StudyMember studyMember1 = studyMemberRepository.save(StudyMember.builder().
                user(user1).study(study1).exitedAt(LocalDateTime.now().plusMonths(1)).
                role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build());

        Announcement announcement = announcementRepository.save(Announcement.builder().study(study1).
                author(studyMember1).title("TestTitle1").build());


        //삭제 예약된 파일
        fileToDelete1 = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey1")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(true)
                .announcement(announcement)
                .build()
        );
        fileToDelete2 = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey2")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(true)
                .announcement(announcement)
                .build()
        );
        //삭제 예약되지 않은 파일
        fileToKeep = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey3")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(false)
                .announcement(announcement)
                .build()
        );
    }

    @Test
    @DisplayName("스케줄러가 지정된 시간에 예약된 삭제 파일들을 DB와 S3에서 삭제")
    void deleteFiles_Db_S3_success(){
        //given
        MockMultipartFile dummy1 = new MockMultipartFile(
                "file",
                "dummy.txt",
                "text/plain",
                "dummyData".getBytes());

        MockMultipartFile dummy2 = new MockMultipartFile(
                "file",
                "dummy.txt",
                "text/plain",
                "dummyData".getBytes());
        MockMultipartFile dummy3 = new MockMultipartFile(
                "file",
                "dummy.txt",
                "text/plain",
                "dummyData".getBytes());

        s3Uploader.uploadFile(fileToDelete1.getFileKey(), dummy1);
        s3Uploader.uploadFile(fileToDelete2.getFileKey(), dummy2);
        s3Uploader.uploadFile(fileToKeep.getFileKey(), dummy3);


        //when
        scheduler.deleteFiles();

        //then
        Assertions.assertThat(fileRepository.findById(fileToDelete1.getId())).isEmpty();
        Assertions.assertThat(fileRepository.findById(fileToDelete2.getId())).isEmpty();
        Assertions.assertThat(fileRepository.findById(fileToKeep.getId())).isPresent();
        Assertions.assertThat(fileRepository.findAll().size()).isEqualTo(1);
        Assertions.assertThat(fileRepository.findAll().get(0).getId()).isEqualTo(fileToKeep.getId());
        //삭제 예약되지 않은 파일은 여전히 존재
        Assertions.assertThat(s3Uploader.getUrlFile(fileToKeep.getFileKey())).isNotNull();
    }


}