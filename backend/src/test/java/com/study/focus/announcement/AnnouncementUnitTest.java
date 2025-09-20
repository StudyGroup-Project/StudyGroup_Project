package com.study.focus.announcement;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.study.repository.StudyMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnnouncementUnitTest {
    @Mock
    private AnnouncementRepository announcementRepo;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    @DisplayName("성공: 스터디 멤버가 공지사항 목록을 성공적으로 조회")
    void findAllSummaries_Success() {
        // given
        Long studyId = 1L;
        Long userId = 100L;

        Announcement a1 = Announcement.builder().id(1L).title("list1").build();
        Announcement a2 = Announcement.builder().id(2L).title("list2").build();
        List<Announcement> announcements = List.of(a1, a2);
        given(studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)).willReturn(true);
        given(announcementRepo.findAllByStudyId(studyId)).willReturn(announcements);

        // when
        List<GetAnnouncementsResponse> result = announcementService.findAllSummaries(studyId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    @DisplayName(" 성공: 공지사항이 없을 경우 빈 리스트를 반환")
    void findAllSummaries_Success_EmptyList() {
        // given
        Long studyId = 1L;
        Long userId = 100L;

        given(studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)).willReturn(true);
        given(announcementRepo.findAllByStudyId(studyId)).willReturn(List.of()); // 빈 리스트 반환 설정

        // when
        List<GetAnnouncementsResponse> result = announcementService.findAllSummaries(studyId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName(" 실패: 스터디 멤버가 아닐 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NotStudyMember() {
        // given
        Long studyId = 1L;
        Long userId = 999L;
        given(studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)).willReturn(false);
        assertThatThrownBy(() -> announcementService.findAllSummaries(studyId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("studyId가 null일 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NullStudyId() {
        Long userId = 100L;

        assertThatThrownBy(() -> announcementService.findAllSummaries(null, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName(" userId가 null일 경우 BusinessException이 발생")
    void findAllSummaries_Fail_NullUserId() {
        // given
        Long studyId = 1L;

        // when & then
        assertThatThrownBy(() -> announcementService.findAllSummaries(studyId, null))
                .isInstanceOf(BusinessException.class);
    }

}