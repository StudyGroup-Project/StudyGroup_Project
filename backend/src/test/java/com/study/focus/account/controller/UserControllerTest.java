package com.study.focus.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.dto.InitUserProfileRequest;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private FileRepository fileRepository;

    @MockBean// 실제 구현 대신 Mock 사용
    private S3Uploader s3Uploader;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .trustScore(50L)
                .lastLoginAt(LocalDateTime.now())
                .build());
    }

    @AfterEach
    void tearDown() {
        userProfileRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 초기 프로필 설정")
    void initProfile_success() throws Exception {
        InitUserProfileRequest request = new InitUserProfileRequest(
                "홍길동", "서울특별시", "강남구",
                "2000-01-01", Job.STUDENT, Category.IT
        );

        mockMvc.perform(post("/api/users/me/profile/basic")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        assertThat(userProfileRepository.findByUserId(user1.getId())).isPresent();
    }

    @Test
    @DisplayName("실패: 초기 프로필 설정 - 잘못된 Job Enum")
    void initProfile_fail_invalidJob() throws Exception {
        String payload = """
            {
              "nickname": "홍길동",
              "province": "서울특별시",
              "district": "강남구",
              "birthDate": "2000-01-01",
              "job": "INVALID",
              "preferredCategory": "IT"
            }
            """;

        mockMvc.perform(post("/api/users/me/profile/basic")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 프로필 이미지 업로드")
    void setProfileImage_success() throws Exception {

        userProfileRepository.save(UserProfile.create(
                user1,
                "홍길동",
                new Address("서울특별시", "강남구"),
                LocalDate.of(2000, 1, 1),
                Job.STUDENT,
                Category.IT
        ));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "dummy-image".getBytes()
        );

        // S3Uploader 동작 Mocking
        FileDetailDto fakeMeta = new FileDetailDto("profile.png", "fake-key", "image/png", 100L);
        given(s3Uploader.makeMetaData(any(MultipartFile.class))).willReturn(fakeMeta);
        willDoNothing().given(s3Uploader).uploadFile(eq("fake-key"), any(MultipartFile.class));
        given(s3Uploader.getUrlFile("fake-key")).willReturn("http://localhost/fake-url");

        mockMvc.perform(multipart("/api/users/me/profile/image")
                        .file(file)
                        .with(user(new CustomUserDetails(user1.getId())))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("http://localhost/fake-url"));
    }

    @Test
    @DisplayName("실패: 프로필 이미지 업로드 - 프로필 없음")
    void setProfileImage_fail_noProfile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", "image/png", "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/profile/image")
                        .file(file)
                        .with(user(new CustomUserDetails(999L))) // 존재하지 않는 유저
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("성공: 내 프로필 조회")
    void getMyProfile_success() throws Exception {
        // 프로필 생성
        InitUserProfileRequest request = new InitUserProfileRequest(
                "홍길동", "서울특별시", "강남구",
                "2000-01-01", Job.STUDENT, Category.IT
        );
        mockMvc.perform(post("/api/users/me/profile/basic")
                        .with(user(new CustomUserDetails(user1.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/profile")
                        .with(user(new CustomUserDetails(user1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("홍길동"))
                .andExpect(jsonPath("$.job").value("STUDENT"))
                .andExpect(jsonPath("$.preferredCategory").value("IT"));
    }

    @Test
    @DisplayName("실패: 내 프로필 조회 - 프로필 없음")
    void getMyProfile_fail_notFound() throws Exception {
        mockMvc.perform(get("/api/users/me/profile")
                        .with(user(new CustomUserDetails(user1.getId()))))
                .andExpect(status().isNotFound());
    }
}