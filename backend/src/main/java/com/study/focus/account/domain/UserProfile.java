package com.study.focus.account.domain;

import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.BaseUpdatedEntity;
import com.study.focus.common.domain.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfile extends BaseUpdatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category preferredCategory;

    @Column(nullable = false, length = 512)
    @Builder.Default
    private String profileImageUrl = "https://example.com/default_profile.png";
}
