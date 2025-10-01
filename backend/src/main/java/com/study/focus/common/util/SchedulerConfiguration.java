package com.study.focus.common.util;

import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulerConfiguration {
    private final FileRepository   fileRepository;
    private final  S3Uploader s3Uploader;


    //매일 자정에 삭제로 설정된 파일을  삭제
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteFiles(){
        log.info("SchedulerConfiguration deleteFiles start");
        fileRepository.findAllByIsDeletedTrue()
                .forEach(file -> {
                    try {
                        s3Uploader.deleteFile(file.getFileKey());
                        fileRepository.delete(file);
                    }catch (BusinessException e){
                        log.error("Scheduler deleteFile S3 error Key: {}",file.getFileKey());
                    }
                    catch (Exception e){
                        log.error("Scheduler deleteFile DataBase Error: {}",file.getFileKey(),e);
                    }
                });
        log.info("SchedulerConfiguration deleteFiles end");
    }
}
