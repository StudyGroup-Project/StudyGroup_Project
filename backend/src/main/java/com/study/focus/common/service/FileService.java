package com.study.focus.common.service;


import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {
    private final FileRepository fileRepository;


    // 특정 공지에 모든 파일 S3과 DB에서 삭제 (예약)
    public void deleteFilesByAnnouncementId(Long announcementId) {
        List<File> files = fileRepository.findAllByAnnouncement_Id(announcementId);
        if (files != null && !files.isEmpty()) {
            files.forEach(File::deleteAnnouncementFile);
            fileRepository.saveAll(files); // 변경 사항 저장
        }
    }

    // 특정 과제에 모든 파일 S3과 DB에서 삭제 (예약)
    public void deleteFilesByAssignmentId(Long assignmentId) {
        List<File> files = fileRepository.findAllByAssignmentId(assignmentId);
        if (files != null && !files.isEmpty()) {
            files.forEach(File::deleteAssignmentFile);
            fileRepository.saveAll(files); // 변경 사항 저장
        }
    }

    // 특정 제출물 목록에 모든 파일 S3과 DB에서 삭제 (예약)
    public void deleteFilesBySubmissionIds(List<Long> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty()) return;
        List<File> files = fileRepository.findAllBySubmissionIdIn(submissionIds);
        if (files != null && !files.isEmpty()) {
            files.forEach(File::deleteSubmissionFile);
            fileRepository.saveAll(files); // 변경 사항 저장
        }
    }

    // 특정 자료에 모든 파일 S3과 DB에서 삭제 (예약)
    public void deleteFilesByResourceId(Long resourceId) {
        // FileRepository에 findAllByResourceId가 있으므로 그것을 사용합니다.
        List<File> files = fileRepository.findAllByResourceId(resourceId);
        if (files != null && !files.isEmpty()) {
            files.forEach(File::deleteResourceFile);
            fileRepository.saveAll(files); // 변경 사항 저장
        }
    }
}