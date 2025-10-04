package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    void deleteAllBySubmission_Assignment_Study_Id(Long studyId);
}
