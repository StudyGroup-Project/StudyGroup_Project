package com.study.focus.assignment.service;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.CreateSubmissionRequest;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FileRepository fileRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final S3Uploader s3Uploader;

    // 과제 제출하기
    @Transactional
    public Long submitSubmission(Long studyId, Long assignmentId, Long userId, CreateSubmissionRequest dto) {
        if(studyId == null || assignmentId == null || userId == null)
            throw new BusinessException(CommonErrorCode.INVALID_PARAMETER);

        StudyMember submitter = studyMemberRepository.findByStudyIdAndUserId(studyId,userId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        Assignment assignment = assignmentRepository.findByIdAndStudyId(assignmentId,studyId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        //마감기한이 지났는지 확인
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueAt = assignment.getDueAt();
        if(dueAt != null && !now.isBefore(dueAt)) throw new BusinessException(CommonErrorCode.INVALID_REQUEST);

        //과제를 이미 제출했는지 확인
        if(submissionRepository.existsByAssignmentIdAndSubmitterId(assignmentId,submitter.getId())) throw new BusinessException(CommonErrorCode.INVALID_REQUEST);

        Submission submission = Submission.builder().assignment(assignment).submitter(submitter).description(dto.getDescription()).build();

        submissionRepository.save(submission);


        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            List<FileDetailDto> list = dto.getFiles().stream().map(s3Uploader::makeMetaData).toList();
            IntStream.range(0, list.size())
                    .forEach(index -> fileRepository.save(
                            File.ofSubmission(submission, list.get(index))
                    ));
            List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys, dto.getFiles());
        }

        return submission.getId();
    }

    // 과제 제출물 상세 데이터 가져오기
    public void getSubmissionDetail(Long studyId, Long assignmentId, Long submissionId) {
        // TODO: 제출물 상세 조회
    }
}
