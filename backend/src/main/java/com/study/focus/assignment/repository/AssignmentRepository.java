package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.dto.CreateAssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByStudyIdOrderByCreatedAtDesc(Long studyId);

    Optional<Assignment> findByIdAndStudyId(Long id, Long studyId);


}
