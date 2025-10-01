package com.study.focus.assignment.repository;

import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.SubmissionListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}
