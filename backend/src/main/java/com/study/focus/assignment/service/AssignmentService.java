package com.study.focus.assignment.service;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.dto.AssignmentCreateRequestDTO;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;

    // 과제 목록 가져오기
    public void getAssignments(Long studyId) {
        // TODO: 과제 목록 조회
    }

    // 과제 생성하기
    @Transactional
    public Long createAssignment(Long studyId, Long creatorId, AssignmentCreateRequestDTO dto) {
        // TODO: 과제 생성
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        StudyMember creator = studyMemberRepository.findByStudyIdAndUserId(studyId, creatorId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        if(!creator.getRole().equals(StudyRole.LEADER)) throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        if(!dto.getDueAt().isAfter(dto.getStartAt())) throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        
        Assignment assignment = Assignment.builder()
                .creator(creator)
                .startAt(dto.getStartAt())
                .dueAt(dto.getDueAt())
                .study(study)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();

        Assignment saveAssignment = assignmentRepository.save(assignment);

        if(dto.getFiles() != null && !dto.getFiles().isEmpty()){
            List<FileDetailDto> list = dto.getFiles().stream().map(s3Uploader::makeMetaData).toList();
            IntStream.range(0,list.size())
                    .forEach(index ->fileRepository.save(
                            File.ofAssignment(assignment,list.get(index))
                    ));
            List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys,dto.getFiles());
        }

        return saveAssignment.getId();
    }

    // 과제 상세 내용 가져오기
    public void getAssignmentDetail(Long studyId, Long assignmentId) {
        // TODO: 과제 상세 조회
    }

    // 과제 수정하기
    public void updateAssignment(Long studyId, Long assignmentId) {
        // TODO: 과제 수정
    }

    // 과제 삭제하기
    public void deleteAssignment(Long studyId, Long assignmentId) {
        // TODO: 과제 삭제
    }
}
