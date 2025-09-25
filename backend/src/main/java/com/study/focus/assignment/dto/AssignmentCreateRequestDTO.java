package com.study.focus.assignment.dto;

import com.study.focus.study.domain.StudyMember;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssignmentCreateRequestDTO {
    StudyMember creator;
    String title;
    String description;
    LocalDateTime startAt;
    LocalDateTime dueAt;
}
