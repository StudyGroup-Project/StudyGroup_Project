package com.study.focus.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetSubmissionDetailResponse {
    Long id;
    String submitterName;
    String description;
    String submitterProfileUrl;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime CreateAt;

    List<AssignmentFileResponse> files;
}
