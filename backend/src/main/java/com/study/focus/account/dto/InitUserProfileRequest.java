package com.study.focus.account.dto;

import com.study.focus.account.domain.Job;
import com.study.focus.common.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class InitUserProfileRequest {
    @NotBlank
    private String nickname;

    @NotBlank
    private String province;

    @NotBlank
    private String district;

    @NotBlank
    private String birthDate;

    @NotNull
    private Job job;

    @NotNull
    private List<Category> preferredCategory;
}
