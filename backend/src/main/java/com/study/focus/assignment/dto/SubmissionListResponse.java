package com.study.focus.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubmissionListResponse {
    Long id;
    Long submitterId;
    String submitterNickname;
    LocalDateTime createdAt;
}
