package com.study.focus.assignment.domain;

import com.study.focus.common.domain.BaseTimeEntity;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Assignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private StudyMember creator;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    public void update(String title, String description, LocalDateTime startAt, LocalDateTime dueAt) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.dueAt = dueAt;
    }
}
