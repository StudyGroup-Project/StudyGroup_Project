package com.study.focus.account.service;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.dto.InitUserProfileRequest;
import com.study.focus.account.dto.UpdateUserProfileRequest;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private S3Uploader s3Uploader;
    @Mock private FileRepository fileRepository;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("초기 프로필 설정 성공")
    void initProfile_success() {
        // given
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        InitUserProfileRequest request = new InitUserProfileRequest(
                "홍길동", "경상북도", "경산시",
                "1999-07-15", Job.STUDENT, List.of(Category.IT)
        );

        // when
        userService.initProfile(1L, request);

        // then
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("프로필 이미지 등록 성공")
    void setProfileImage_success() {
        // given
        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder().user(user).nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(1999, 7, 15))
                .job(Job.STUDENT).preferredCategory(List.of(Category.IT))
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        MultipartFile mockFile = mock(MultipartFile.class);
        FileDetailDto meta = new FileDetailDto("hong.png", "key-123", "image/png", 123L);

        File savedFile = File.ofProfileImage(meta);
        when(fileRepository.save(any(File.class))).thenReturn(savedFile);

        when(s3Uploader.makeMetaData(mockFile)).thenReturn(meta);
        doNothing().when(s3Uploader).uploadFile(anyString(), any());
        when(s3Uploader.getUrlFile("key-123")).thenReturn("https://cdn.example.com/hong.png");

        // when
        String url = userService.setProfileImage(1L, mockFile);

        // then
        assertThat(url).isEqualTo("https://cdn.example.com/hong.png");
        assertThat(profile.getProfileImage()).isNotNull();
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 시 예외 발생")
    void setProfileImage_uploadFail() {
        // given
        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder().user(user).nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(1999, 7, 15))
                .job(Job.STUDENT).preferredCategory(List.of(Category.IT))
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        MultipartFile mockFile = mock(MultipartFile.class);
        FileDetailDto meta = new FileDetailDto("hong.png", "key-123", "image/png", 123L);

        when(s3Uploader.makeMetaData(mockFile)).thenReturn(meta);
        doThrow(new RuntimeException("S3 error")).when(s3Uploader).uploadFile(anyString(), any());

        // when & then
        assertThatThrownBy(() -> userService.setProfileImage(1L, mockFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.FILE_UPLOAD_FAIL.getMessage());
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_success() {
        // given
        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder().user(user).nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(1999, 7, 15))
                .job(Job.STUDENT).preferredCategory(List.of(Category.IT))
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "임꺽정", "서울특별시", "강남구", "2000-01-01", Job.FREELANCER, List.of(Category.LANGUAGE)
        );

        // when
        userService.updateProfile(1L, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("임꺽정");
        assertThat(profile.getAddress().getProvince()).isEqualTo("서울특별시");
    }

    @Test
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_success() {
        // given
        User user = User.builder().id(1L).trustScore(80L).build();
        FileDetailDto meta = FileDetailDto.builder()
                .originalFileName("hong.png")
                .key("key-123")
                .contentType("image/png")
                .fileSize(100L)
                .build();

        File file = File.ofProfileImage(meta);

        UserProfile profile = UserProfile.builder().user(user).nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(1999, 7, 15))
                .job(Job.STUDENT).preferredCategory(List.of(Category.IT))
                .profileImage(file)
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(s3Uploader.getUrlFile("key-123")).thenReturn("https://cdn.example.com/hong.png");

        // when
        GetMyProfileResponse response = userService.getMyProfile(1L);

        // then
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://cdn.example.com/hong.png");
        assertThat(response.getTrustScore()).isEqualTo(80L);
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 프로필 없음")
    void getMyProfile_profileNotFound() {
        // given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyProfile(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.PROFILE_NOT_FOUND.getMessage());
    }
}
