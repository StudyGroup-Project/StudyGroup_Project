package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.SubmissionListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("""
    select new com.study.focus.assignment.dto.SubmissionListResponse(
        s.id,
        u.id,
        p.nickname,
        s.createdAt
    )
    from Submission s
      join s.submitter sm
      join sm.user u
      left join UserProfile p on p.user = u
    where s.assignment.id = :assignmentId
    order by s.createdAt desc
    """)
    List<SubmissionListResponse> findSubmissionList(Long assignmentId);

    List<Submission> findAllByAssignmentId(Long assignmentId);

    boolean existsByAssignmentIdAndSubmitterId(Long assignmentId, Long submitterId);

    @Query("SELECT s.id FROM Submission s WHERE s.assignment.id = :assignmentId")
    List<Long> findIdsByAssignmentId(@Param("assignmentId") Long assignmentId);

    void deleteAllByAssignment_Study_Id(Long studyId);

    Optional<Submission> findByIdAndAssignmentId(Long submissionId, Long assignmentId);
}
