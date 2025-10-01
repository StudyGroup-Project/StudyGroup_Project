package com.study.focus.application.domain;

import com.study.focus.account.domain.User;
import com.study.focus.common.domain.BaseCreatedEntity;
import com.study.focus.study.domain.Study;
import jakarta.persistence.*;
import lombok.*;

import static com.study.focus.application.domain.ApplicationStatus.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Application extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = SUBMITTED;

    @Lob
    @Column(nullable = false)
    private String content;

    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
    }
}
