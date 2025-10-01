package com.study.focus.study.repository;

import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyProfileRepository extends JpaRepository<StudyProfile, Long> {
    Optional<StudyProfile> findByStudy(Study study);
    Optional<StudyProfile> findByStudyId(Long studyId);
}
