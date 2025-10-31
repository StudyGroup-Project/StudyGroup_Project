package com.study.focus.study.service;

import com.study.focus.common.dto.StudyDto;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyProfile;
import com.study.focus.study.domain.StudySortType;
import com.study.focus.study.dto.GetStudiesResponse;
import com.study.focus.study.dto.SearchStudiesRequest;
import com.study.focus.study.dto.SearchStudiesResponse;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
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
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private StudyQueryService studyQueryService;

    @Test
    void searchStudies_mapsRepositoryResultToResponse() {
        StudyDto dto = new StudyDto(1L, "알고리즘", 10, 5, 3,
                "PS", List.of(Category.IT), 80, true);

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

    @Test
    void myStudies_mapsRepositoryResultToResponse(){
        Long userId = 1L;

        StudyProfile sp = mock(StudyProfile.class);
        Study study = mock(Study.class);
        when(sp.getStudy()).thenReturn(study);
        when(study.getId()).thenReturn(100L);
        when(sp.getTitle()).thenReturn("알고리즘");


        when(studyMemberRepository.countByStudyIdAndStatus(eq(100L), eq(StudyMemberStatus.JOINED)))
                .thenReturn(0L);
        when(bookmarkRepository.countByStudyId(100L)).thenReturn(0L);
        when(studyMemberRepository.findLeaderTrustScoreByStudyId(100L))
                .thenReturn(java.util.Optional.empty());
        when(studyRepository.findJoinedStudyProfiles(userId)).thenReturn(List.of(sp));

        GetStudiesResponse res = studyQueryService.getMyStudies(userId);

        assertThat(res.getStudies()).hasSize(1);
        assertThat(res.getStudies().get(0).getTitle()).isEqualTo("알고리즘");

        verify(studyRepository).findJoinedStudyProfiles(eq(userId));
    }

    @Test
    void myBookmarkedStudies_mapsRepositoryResultToResponse() {
        Long userId = 1L;

        StudyProfile sp = mock(StudyProfile.class);
        Study study = mock(Study.class);
        when(sp.getStudy()).thenReturn(study);
        when(study.getId()).thenReturn(200L);
        when(sp.getTitle()).thenReturn("자료구조");

        when(studyMemberRepository.countByStudyIdAndStatus(200L, StudyMemberStatus.JOINED))
                .thenReturn(0L);
        when(bookmarkRepository.countByStudyId(200L)).thenReturn(1L);
        when(studyMemberRepository.findLeaderTrustScoreByStudyId(200L))
                .thenReturn(java.util.Optional.empty());
        when(studyRepository.findBookmarkedStudyProfiles(userId)).thenReturn(List.of(sp));


        GetStudiesResponse res = studyQueryService.getBookmarks(userId);

        assertThat(res.getStudies()).hasSize(1);
        assertThat(res.getStudies().get(0).getTitle()).isEqualTo("자료구조");
        assertThat(res.getStudies().get(0).isBookmarked()).isTrue();

        verify(studyRepository).findBookmarkedStudyProfiles(eq(userId));
    }
}