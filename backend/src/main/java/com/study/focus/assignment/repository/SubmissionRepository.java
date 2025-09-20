package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}