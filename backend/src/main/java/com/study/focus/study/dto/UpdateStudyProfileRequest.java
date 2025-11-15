package com.study.focus.study.dto;


import com.study.focus.common.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdateStudyProfileRequest {
    private String title;
    private int maxMemberCount;
    private List<Category> category;
    private String province;
    private String district;
    private String bio;
    private String description;

}
