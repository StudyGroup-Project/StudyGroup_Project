package com.study.focus.common.config;

import com.study.focus.common.domain.File;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class SchedulerConfigurationTest {

    @Autowired
    private SchedulerConfiguration schedulerConfiguration;

    @Autowired
    private FileRepository fileRepository;

    @Mock
    private S3Uploader s3Uploader;


    private File fileToDelete1;
    private File fileToDelete2;
    private File fileToKeep;

    @BeforeEach
    void setUp(){
        fileToDelete1 = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(true)
                .build()
        );
        fileToDelete2 = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(true)
                .build()
        );
        fileToKeep = fileRepository.save(File.builder()
                .fileName("testName")
                .fileKey("Testkey")
                .mimeType("TestType")
                .fileSize(30L)
                .isDeleted(false)
                .build()
        );
    }

    @Test
    @DisplayName("스케줄러가 지정된 시간에 예약 삭제 파일들을 DB와 S3에서 삭제")
    void deleteFiles_Db_S3_success(){
        schedulerConfiguration.deleteFiles();
        verify(s3Uploader, times(1)).deleteFile(fileToDelete1.getFileKey());
        verify(s3Uploader, times(1)).deleteFile(fileToDelete2.getFileKey());
        verify(s3Uploader, times(0)).deleteFile(fileToKeep.getFileKey());

        // 6. DB 상태를 직접 확인하여 데이터가 올바르게 변경되었는지 검증
        List<File> allFiles = fileRepository.findAll();
        assertThat(allFiles).hasSize(1); // 삭제되지 않아야 할 파일 1개만 남았는지 확인
        assertThat(allFiles.get(0).getFileKey()).isEqualTo("TestKey");
    }


}