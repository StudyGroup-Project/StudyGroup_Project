package com.study.focus.study.domain;

import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.BaseUpdatedEntity;
import com.study.focus.common.domain.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyProfile extends BaseUpdatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false, unique = true)
    private Study study;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 255)
    private String bio;

    @Lob
    private String description;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    public void update(String title, Category category, Address address, String bio, String description) {
        this.title = title;
        this.category = category;
        this.address = address;
        this.bio = bio;
        this.description = description;
    }
}
