package com.study.focus.assignment.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.assignment.dto.CreateAssignmentRequest;
import com.study.focus.assignment.dto.GetAssignmentDetailResponse;
import com.study.focus.assignment.dto.GetAssignmentsResponse;
import com.study.focus.assignment.dto.UpdateAssignmentRequest;
import com.study.focus.assignment.service.AssignmentService;
import com.study.focus.study.domain.StudyMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 과제 목록 가져오기
    @GetMapping
    public ResponseEntity<List<GetAssignmentsResponse>> getAssignments(@PathVariable Long studyId,
                                                                       @AuthenticationPrincipal CustomUserDetails user)
    {
        Long userId = user.getUserId();
        return ResponseEntity.ok(assignmentService.getAssignments(studyId,userId));
    }

    // 과제 생성하기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createAssignment(@PathVariable Long studyId,
                                                 @ModelAttribute @Valid CreateAssignmentRequest dto,
                                                 @AuthenticationPrincipal CustomUserDetails user)
    {
        Long creator = user.getUserId();
        Long createAssginmnetId = assignmentService.createAssignment(studyId,creator,dto);
        URI location = URI.create("/api/studies/" + studyId + "/assignments/" + createAssginmnetId);
        return ResponseEntity.created(location).build();
    }

    // 과제 상세 내용 가져오기
    @GetMapping("/{assignmentId}")
    public ResponseEntity<GetAssignmentDetailResponse> getAssignmentDetail(@PathVariable Long studyId, @PathVariable Long assignmentId,
                                                                           @AuthenticationPrincipal CustomUserDetails user)
    {
        Long userId = user.getUserId();
        return ResponseEntity.ok(assignmentService.getAssignmentDetail(studyId,assignmentId,userId));
    }

    // 과제 수정하기
    @PutMapping(value = "/{assignmentId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAssignment(@PathVariable Long studyId,
                                 @PathVariable Long assignmentId,
                                 @ModelAttribute @Valid UpdateAssignmentRequest dto,
                                 @AuthenticationPrincipal CustomUserDetails user) {
        Long creator = user.getUserId();
        assignmentService.updateAssignment(studyId,assignmentId,creator,dto);
        return ResponseEntity.ok().build();
    }

    // 과제 삭제하기
    @DeleteMapping("/{assignmentId}")
    public void deleteAssignment(@PathVariable Long studyId, @PathVariable Long assignmentId) {}
}
