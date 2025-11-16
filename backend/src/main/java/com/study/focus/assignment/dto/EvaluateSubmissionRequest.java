package com.study.focus.assignment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateSubmissionRequest {
    @NotNull @Min(-5) @Max(5)
    Long score;
    String content;
}
