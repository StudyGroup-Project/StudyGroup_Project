package com.study.focus.announcement.repository;

import com.study.focus.announcement.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
