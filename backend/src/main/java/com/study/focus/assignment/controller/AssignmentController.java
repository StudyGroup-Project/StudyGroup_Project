package com.study.focus.assignment.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments")
public class AssignmentController {

    // 과제 목록 가져오기
    @GetMapping
    public void getAssignments(@PathVariable Long studyId) {}

    // 과제 생성하기
    @PostMapping
    public void createAssignment(@PathVariable Long studyId) {}

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