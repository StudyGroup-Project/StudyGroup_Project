package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByStudyIdOrderByCreatedAtDesc(Long studyId);
}
