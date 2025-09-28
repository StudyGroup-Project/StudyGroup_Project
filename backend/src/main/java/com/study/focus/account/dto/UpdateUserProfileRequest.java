package com.study.focus.account.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class UpdateUserProfileRequest {
    private String nickname;
    private String province;
    private String district;
    private String birthDate;
    private String job;
    private String preferredCategory;
}
