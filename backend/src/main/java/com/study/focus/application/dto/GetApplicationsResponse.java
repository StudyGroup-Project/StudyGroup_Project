package com.study.focus.application.dto;


import com.study.focus.application.domain.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GetApplicationsResponse {

    private Long applicationId;
    private Long applicantId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createAt;
    private ApplicationStatus status;

}
