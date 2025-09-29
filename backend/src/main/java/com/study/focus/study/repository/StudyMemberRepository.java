package com.study.focus.study.repository;

import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember,Long> {
   Optional<StudyMember> findByStudyIdAndUserId(Long study_id, Long user_id);

   Optional<StudyMember> findByStudyIdAndRole(Long studyId, StudyRole role);

   long countByStudyIdAndStatus(Long studyId, StudyMemberStatus status);

   boolean existsByStudyIdAndUserIdAndStatus(Long studyId, Long userId, StudyMemberStatus status);


}