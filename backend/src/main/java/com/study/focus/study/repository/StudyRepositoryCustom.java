package com.study.focus.study.repository;

import com.study.focus.common.domain.Category;
import com.study.focus.common.dto.StudyDto;
import com.study.focus.study.domain.StudySortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryCustom {
    Page<StudyDto> searchStudies(String keyword,
                                 Category category,
                                 String province,
                                 String district,
                                 Long userId,
                                 StudySortType sortType,
                                 Pageable pageable);
}