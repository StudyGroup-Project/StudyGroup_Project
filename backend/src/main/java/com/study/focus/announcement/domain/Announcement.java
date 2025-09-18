package com.study.focus.announcement.domain;

import com.study.focus.common.domain.BaseTimeEntity;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private StudyMember author;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String description;
}
