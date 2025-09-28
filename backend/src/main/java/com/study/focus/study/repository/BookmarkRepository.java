package com.study.focus.study.repository;

import com.study.focus.account.domain.User;
import com.study.focus.study.domain.Bookmark;
import com.study.focus.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndStudy(User user, Study study);
}
