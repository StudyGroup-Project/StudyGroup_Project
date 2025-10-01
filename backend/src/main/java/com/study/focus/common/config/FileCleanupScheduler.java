package com.study.focus.common.config;

import com.study.focus.common.domain.File;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileCleanupScheduler {
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;


    //매일 새벽 2시에 삭제 예약된 파일을 삭제
    //20회 이상 실패시 DB 문제 혹은 S3 문제로 판단하여 관리자가 빠른 조치할 수 있도록 중단
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void deleteFiles() {
        int countLimit = 20;
        log.info("Scheduler File Deletion start");
        for (File file : fileRepository.findAllByIsDeletedTrue()) {
            try {
                s3Uploader.deleteFile(file.getFileKey());
                fileRepository.delete(file);
                log.info("Scheduler deleteFile Success: {}", file.getFileKey());
            } catch (BusinessException e) {
                log.error("Scheduler deleteFile S3 Error: {}", file.getFileKey(), e);
                countLimit--;
            } catch (Exception e) {
                log.error("Scheduler deleteFile Error: {}", file.getFileKey(), e);
                countLimit--;
            }
            if (countLimit <= 0) {
                log.error("Scheduler deleteFile stopped due to too many errors");
                break; // 전체 루프 중단 가능
            }
        }
        log.info("Scheduler File Deletion end");
    }

    }