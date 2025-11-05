package com.study.focus.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GetFeedbackListResponse {
    Long id;
    Long score;
    String feedback;
    LocalDateTime evaluatedAt;
    String evaluatorName;
    String evaluatorProfileUrl;
}
