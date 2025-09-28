package com.study.focus.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class InitProfileRequest {
    @NotBlank
    private String nickname;
    @NotBlank
    private String province;
    @NotBlank
    private String district;
    @NotBlank
    private String birthDate;
    @NotBlank
    private String job;
    @NotBlank
    private String preferredCategory;
}
