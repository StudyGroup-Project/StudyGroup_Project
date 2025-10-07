package com.study.focus.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GetFeedbackListResponse {
    Long id;
    Long score;
    String feedback;
    LocalDateTime evaluatedAt;
    String evaluaterName;
    String evaluatorProfileUrl;
}
