package com.study.focus.announcement.repository;

import com.study.focus.announcement.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAnnouncement_Id(Long announcement_id);

    void deleteAllByAnnouncement_Study_Id(Long studyId);
}
