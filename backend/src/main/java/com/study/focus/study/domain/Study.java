package com.study.focus.study.domain;

import com.study.focus.common.domain.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Study extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int maxMemberCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecruitStatus recruitStatus = RecruitStatus.OPEN;

    public void updateMaxMemberCount(int maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }
}
