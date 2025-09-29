package com.study.focus.application.repository;

import com.study.focus.account.domain.User;
import com.study.focus.application.domain.Application;
import com.study.focus.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByApplicantAndStudy(User applicant, Study study);

    Optional<Application> findByApplicantIdAndStudyId(Long applicantId, Long studyId);
}