package com.study.focus.notification.repository;

import com.study.focus.notification.domain.Notification;
import com.study.focus.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteAllByStudy_Id(Long studyId);

    List<Notification> findAllByStudy(Study study);
}
