package com.study.focus.study.dto;

import com.study.focus.common.dto.StudyDto;
import com.study.focus.study.domain.StudySortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetStudiesResponse {
    List<StudyDto> studies;
}
