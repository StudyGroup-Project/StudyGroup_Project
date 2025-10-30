package com.study.focus.account.dto;

import com.study.focus.account.domain.Job;
import com.study.focus.common.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String nickname;
    private String province;
    private String district;
    private String birthDate;
    private Job job;
    private List<Category> preferredCategory;
}
