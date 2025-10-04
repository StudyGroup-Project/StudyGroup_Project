package com.study.focus.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetStudyMembersResponse {
    private Long studyId;
    List<StudyMemberDto> members;
}
