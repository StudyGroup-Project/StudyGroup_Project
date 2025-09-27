package com.study.focus.study.dto;

import com.study.focus.common.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CreateStudyRequest {

    private String title;
    private Integer maxMemberCount;
    private Category category;
    private String province;    // 시/도
    private String district;    // 시/군/구
    private String bio;         // 짧은 소개
    private String description; // 긴 소개
}
