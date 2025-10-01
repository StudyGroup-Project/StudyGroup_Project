package com.study.focus.study.domain;

import com.study.focus.account.domain.User;
import com.study.focus.common.domain.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.study.focus.study.domain.StudyMemberStatus.*;
import static com.study.focus.study.domain.StudyRole.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_study_member", columnNames = {"user_id", "study_id"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyMember extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StudyMemberStatus status =  JOINED;

    @Column(nullable = true)
    private LocalDateTime exitedAt;
}
