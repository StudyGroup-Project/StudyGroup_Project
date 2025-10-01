package com.study.focus.account.dto;

import com.study.focus.account.domain.Job;
import com.study.focus.common.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class GetMyProfileResponse {
    private Long id;
    private String nickname;
    private String province;
    private String district;
    private String birthDate;
    private Job job;
    private Category preferredCategory;
    private String profileImageUrl;
    private Long trustScore;
}
