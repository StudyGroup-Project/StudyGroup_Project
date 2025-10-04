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
    private final S3Uploader s3Uploader;

    // 특정 공지에 모든 파일 S3과 DB에서 삭제
    public void deleteFilesByAnnouncementId(Long announcementId) {
        List<File> files = fileRepository.findAllByAnnouncement_Id(announcementId);
        deleteFiles(files);
    }

    // 특정 과제에 모든 파일 S3과 DB에서 삭제
    public void deleteFilesByAssignmentId(Long assignmentId) {
        List<File> files = fileRepository.findAllByAssignmentId(assignmentId);
        deleteFiles(files);
    }

    // 특정 제출물 목록에 모든 파일 S3과 DB에서 삭제
    public void deleteFilesBySubmissionIds(List<Long> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty()) return;
        List<File> files = fileRepository.findAllBySubmissionIdIn(submissionIds);
        deleteFiles(files);
    }

    // 특정 자료에 모든 파일 S3과 DB에서 삭제
    public void deleteFilesByResourceId(Long resourceId) {
        List<File> files = fileRepository.findAllByResourceId(resourceId);
        deleteFiles(files);
    }

    // 파일 삭제 로직
    private void deleteFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // S3에서 실제 파일들을 먼저 삭제
        for (File file : files) {
            if (file.getFileKey() != null && !file.getFileKey().isEmpty()) {
                s3Uploader.deleteFile(file.getFileKey());
            }
        }

        //DB에서 파일 엔티티 완전히 삭제
        fileRepository.deleteAllInBatch(files);
    }
}