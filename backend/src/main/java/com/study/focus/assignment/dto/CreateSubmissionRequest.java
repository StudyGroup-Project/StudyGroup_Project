package com.study.focus.assignment.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateSubmissionRequest {
    String description;
    List<MultipartFile> files;
}
