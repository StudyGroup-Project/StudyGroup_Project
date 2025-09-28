package com.study.focus.application.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.RecruitStatus;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;

    // 지원서 제출하기
    public Long submitApplication(Long applicantId, Long studyId, SubmitApplicationRequest request) {

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 스터디가 모집중인지 상태확인
        if (study.getRecruitStatus() != RecruitStatus.OPEN) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // 지원서 reject당했을 때 다시 지원해도 예외처리 안해서 다시 지원할 수 있게
        applicationRepository.findByApplicantAndStudy(applicant, study)
                .ifPresent(application -> {
                    if (application.getStatus() != ApplicationStatus.REJECTED) {
                        throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
                    }
                });


        Application application = Application.builder()
                .applicant(applicant)
                .study(study)
                .content(request.getContent())
                .build();

        Application saveApplication = applicationRepository.save(application);
        return saveApplication.getId();
    }

    // 지원서 목록 가져오기
    public void getApplications(Long studyId) {
        // TODO: 지원서 목록 조회
    }

    // 지원서 상세 가져오기
    public void getApplicationDetail(Long studyId, Long applicationId) {
        // TODO: 지원서 상세 조회
    }

    // 지원서 처리하기 (승인/거절 등)
    public void handleApplication(Long studyId, Long applicationId) {
        // TODO: 지원서 상태 변경
    }
}
