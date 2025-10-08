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
    private Meta meta;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Meta {
        private int page;
        private int limit;
        private long totalCount;
        private int totalPages;
    }
}
