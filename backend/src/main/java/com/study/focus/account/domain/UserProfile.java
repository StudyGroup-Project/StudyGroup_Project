package com.study.focus.account.domain;

import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.BaseUpdatedEntity;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.util.CategoryListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfile extends BaseUpdatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
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

    @Convert(converter = CategoryListConverter.class) // 컨버터 지정
    @Column(nullable = false)
    private List<Category> preferredCategory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private File profileImage;

    /**
     * 초기 프로필 생성
     */
    public static UserProfile create(User user,
                                     String nickname,
                                     Address address,
                                     LocalDate birthDate,
                                     Job job,
                                     List<Category> preferredCategory) {
        return UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .address(address)
                .birthDate(birthDate)
                .job(job)
                .preferredCategory(preferredCategory)
                .build();
    }

    /**
     * 프로필 수정
     */
    public void updateProfile(String nickname,
                              Address address,
                              LocalDate birthDate,
                              Job job,
                              List<Category> preferredCategory) {
        this.nickname = nickname;
        this.address = address;
        this.birthDate = birthDate;
        this.job = job;
        this.preferredCategory = preferredCategory;
    }

    /**
     * 프로필 이미지 교체
     * 기존 파일이 있으면 soft delete 후 교체
     */
    public void updateProfileImage(File newFile) {
        if (this.profileImage != null) {
            this.profileImage.delete(); // File 엔티티의 soft delete 메서드
        }
        this.profileImage = newFile;
    }
}

