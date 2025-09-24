package com.study.focus.announcement.repository;

import com.study.focus.announcement.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByStudyId(Long study_id);
    Optional<Announcement> findByIdAndStudy_IdAndAuthor_Id(Long study_id, Long user_id, Long announcement_id);
}
