package com.study.focus.assignment.dto;

import com.study.focus.study.domain.StudyMember;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AssignmentCreateRequestDTO {
    StudyMember creator;
    String title;
    String description;
    LocalDateTime startAt;
    LocalDateTime dueAt;
    List<MultipartFile> files;
}
