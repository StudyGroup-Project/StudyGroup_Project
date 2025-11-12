package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    void deleteAllBySubmission_Assignment_Study_Id(Long studyId);

    boolean existsBySubmissionIdAndReviewerId(Long submissionId, Long id);

    List<Feedback> findAllBySubmissionIdOrderByCreatedAtDescIdDesc(Long submissionId);
}
