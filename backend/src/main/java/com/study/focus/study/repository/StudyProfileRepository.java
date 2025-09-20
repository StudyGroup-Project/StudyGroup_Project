package com.study.focus.study.repository;

import com.study.focus.study.domain.StudyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyProfileRepository extends JpaRepository<StudyProfile, Long> {
}
