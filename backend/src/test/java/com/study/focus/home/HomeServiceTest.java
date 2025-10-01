package com.study.focus.home;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.home.dto.HomeResponse;
import com.study.focus.home.service.HomeService;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private S3Uploader s3Uploader;
    @Mock private FileRepository fileRepository;

    @InjectMocks private HomeService homeService;

    @Test
    @DisplayName("성공: 홈 데이터 조회")
    void getHomeData_success() {
        // given
        User user = User.builder().id(1L).trustScore(80L).build();
        File file = File.ofProfileImage(new FileDetailDto(
                "profile.png", "test-key", "image/png", 123L));
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(2000, 1, 1))
                .job(Job.STUDENT)
                .preferredCategory(Category.IT)
                .profileImage(file)
                .build();

        Study study = Study.builder().id(10L).maxMemberCount(5).build();
        StudyProfile sp = StudyProfile.builder()
                .study(study)
                .title("백엔드 스터디")
                .bio("백엔드 개발자를 위한 스터디")
                .category(Category.IT)
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(s3Uploader.getUrlFile(anyString())).thenReturn("https://cdn.example.com/hong.png");
        when(studyMemberRepository.findTop10StudyProfiles(PageRequest.of(0, 10)))
                .thenReturn(List.of(sp));
        when(bookmarkRepository.findAllByUserId(1L)).thenReturn(List.of(
                Bookmark.builder().user(user).study(study).build()
        ));
        when(studyMemberRepository.countByStudyIdAndStatus(10L, StudyMemberStatus.JOINED)).thenReturn(2L);
        when(bookmarkRepository.countByStudyId(10L)).thenReturn(31L);
        when(studyMemberRepository.findLeaderTrustScoreByStudyId(10L)).thenReturn(Optional.of(80L));

        // when
        HomeResponse response = homeService.getHomeData(1L);

        // then
        assertThat(response.getUser().getNickname()).isEqualTo("홍길동");
        assertThat(response.getUser().getProfileImageUrl()).isEqualTo("https://cdn.example.com/hong.png");
        assertThat(response.getTopStudies()).hasSize(1);
        assertThat(response.getTopStudies().get(0).getTitle()).isEqualTo("백엔드 스터디");
        assertThat(response.getTopStudies().get(0).getBookmarkCount()).isEqualTo(31);
        assertThat(response.getTopStudies().get(0).isBookmarked()).isTrue();
    }

    @Test
    @DisplayName("실패: 프로필 없음 → BusinessException 발생")
    void getHomeData_fail_profileNotFound() {
        // given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> homeService.getHomeData(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.PROFILE_NOT_FOUND.getMessage());
    }
}
