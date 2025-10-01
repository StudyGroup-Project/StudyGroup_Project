package com.study.focus.common.dto;

import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.StudyProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyDto {
    private Long id;
    private String title;
    private int maxMemberCount;
    private int memberCount;
    private long bookmarkCount;
    private String bio;
    private Category category;
    private long trustScore;
    private boolean bookmarked;

    public static StudyDto of(StudyProfile profile,
                              int memberCount,
                              long bookmarkCount,
                              long trustScore,
                              boolean bookmarked) {
        return new StudyDto(
                profile.getStudy().getId(),
                profile.getTitle(),
                profile.getStudy().getMaxMemberCount(),
                memberCount,
                bookmarkCount,
                profile.getBio(),
                profile.getCategory(),
                trustScore,
                bookmarked
        );
    }
}
