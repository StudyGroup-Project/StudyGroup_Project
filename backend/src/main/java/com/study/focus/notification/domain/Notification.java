package com.study.focus.notification.domain;

import com.study.focus.common.domain.BaseCreatedEntity;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private StudyMember actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudienceType audienceType;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String description;
}
