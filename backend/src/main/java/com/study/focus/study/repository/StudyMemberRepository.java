package com.study.focus.study.repository;

import com.study.focus.study.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember,Long> {
   Optional<StudyMember> findByStudyIdAndUserId(Long study_id, Long user_id);
}