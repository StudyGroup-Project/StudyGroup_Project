package com.study.focus.assignment.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.assignment.dto.AssignmentCreateRequestDTO;
import com.study.focus.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 과제 목록 가져오기
    @GetMapping
    public void getAssignments(@PathVariable Long studyId) {

    }

    // 과제 생성하기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createAssignment(@PathVariable Long studyId,
                                                 @ModelAttribute AssignmentCreateRequestDTO dto,
                                                 @AuthenticationPrincipal CustomUserDetails user)
    {
        Long creator = user.getUserId();
        Long createAssginmnetId = assignmentService.createAssignment(studyId,creator,dto);
        URI location = URI.create("/api/studies/" + studyId + "/assignments/" + createAssginmnetId);
        return ResponseEntity.created(location).build();
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
