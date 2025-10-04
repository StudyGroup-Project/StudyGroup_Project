package com.study.focus.assignment.dto;

import com.study.focus.common.dto.AssignmentFileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GetSubmissionDetailResponse {
    Long id;
    String submitterName;
    String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime CreateAt;

    List<AssignmentFileResponse> files;
}
