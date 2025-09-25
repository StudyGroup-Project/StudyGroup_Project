package com.study.focus.assignment.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.assignment.dto.AssignmentCreateRequestDTO;
import com.study.focus.assignment.service.AssignmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }


    // 과제 목록 가져오기
    @GetMapping
    public void getAssignments(@PathVariable Long studyId) {}

    // 과제 생성하기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createAssignment(@PathVariable Long studyId,
                                                 @ModelAttribute AssignmentCreateRequestDTO dto,
                                                 @AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestPart(name = "files",required = false) List<MultipartFile> files)
    {
        Long creator = user.getUserId();
        assignmentService.createAssignment(studyId,creator,dto,files);
        return ResponseEntity.ok().build();
    }

    // 과제 상세 내용 가져오기
    @GetMapping("/{assignmentId}")
    public void getAssignmentDetail(@PathVariable Long studyId, @PathVariable Long assignmentId) {}

    // 과제 수정하기
    @PutMapping("/{assignmentId}")
    public void updateAssignment(@PathVariable Long studyId, @PathVariable Long assignmentId) {}

    // 과제 삭제하기
    @DeleteMapping("/{assignmentId}")
    public void deleteAssignment(@PathVariable Long studyId, @PathVariable Long assignmentId) {}
}