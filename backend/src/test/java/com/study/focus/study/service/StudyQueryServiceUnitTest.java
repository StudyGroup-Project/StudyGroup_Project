package com.study.focus.study.service;

import com.study.focus.common.dto.StudyDto;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.StudySortType;
import com.study.focus.study.dto.SearchStudiesRequest;
import com.study.focus.study.dto.SearchStudiesResponse;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyQueryServiceUnitTest {

    @Mock
    private StudyRepository studyRepository;

    @InjectMocks
    private StudyQueryService studyQueryService;

    @Test
    void searchStudies_mapsRepositoryResultToResponse() {
        StudyDto dto = new StudyDto(1L, "알고리즘", 10, 5, 3,
                "PS", Category.IT, 80, true);

        Page<StudyDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(studyRepository.searchStudies(any(), any(), any(), any(), anyLong(), any(), any()))
                .thenReturn(page);

        SearchStudiesRequest req = new SearchStudiesRequest();
        req.setKeyword("알고리즘");
        req.setCategory(Category.IT);
        req.setSort(StudySortType.LATEST);

        SearchStudiesResponse res = studyQueryService.searchStudies(req, 1L);

        assertThat(res.getStudies()).hasSize(1);
        assertThat(res.getStudies().get(0).getTitle()).isEqualTo("알고리즘");
        assertThat(res.getMeta().getSort()).isEqualTo(StudySortType.LATEST);
    }
}