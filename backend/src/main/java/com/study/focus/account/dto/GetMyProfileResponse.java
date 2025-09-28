package com.study.focus.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class GetMyProfileResponse {
    private Long id;
    private String nickname;
    private String province;
    private String district;
    private String birthDate;
    private String job;
    private String preferredCategory;
    private String profileImageUrl;
    private Long trustScore;
}
