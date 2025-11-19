package com.study.focus.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
@AllArgsConstructor
public class AssignmentFileResponse {
    private  Long fileId;
    private String fileName;
    private String url;
}
