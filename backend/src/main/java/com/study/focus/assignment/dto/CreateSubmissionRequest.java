package com.study.focus.assignment.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubmissionRequest {
    String description;
    List<MultipartFile> files;
}
