package com.study.focus.account.domain;

import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.BaseUpdatedEntity;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private File profileImage;

    /**
     * 프로필 이미지 교체
     * 기존 파일이 있으면 soft delete 처리 후 새로운 파일로 교체
     */
    public void updateProfileImage(File newFile) {
        if (this.profileImage != null) {
            this.profileImage.delete(); // isDeleted = true
        }
        this.profileImage = newFile;
    }
}
