package com.study.focus.study.dto;


import com.study.focus.common.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateStudyProfileRequest {
    private String title;
    private int maxMemberCount;
    private Category category;
    private String province;
    private String district;
    private String bio;
    private String description;

}
