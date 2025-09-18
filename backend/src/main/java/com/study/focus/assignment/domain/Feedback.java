package com.study.focus.assignment.domain;

import com.study.focus.common.domain.BaseCreatedEntity;
import com.study.focus.study.domain.StudyMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Feedback extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private StudyMember reviewer;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int score;
}
