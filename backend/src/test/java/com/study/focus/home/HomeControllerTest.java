package com.study.focus.home;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HomeControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyProfileRepository studyProfileRepository;
    @Autowired private StudyMemberRepository studyMemberRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private FileRepository fileRepository;

    @MockBean
    private S3Uploader s3Uploader;

    private User user;
    private UserProfile profile;
    private Study study;
    private StudyProfile studyProfile;

    @BeforeEach
    void setUp() {
        // 유저 저장
        user = userRepository.save(User.builder()
                .trustScore(80L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        // 더미 파일 생성
        File file = fileRepository.save(
                File.ofProfileImage(new FileDetailDto(
                        "profile.png",
                        "test-key",
                        "image/png",
                        123L
                ))
        );

        // 유저 프로필 저장 (이미지 포함)
        profile = userProfileRepository.save(UserProfile.builder()
                .user(user)
                .nickname("홍길동")
                .address(new Address("경상북도", "경산시"))
                .birthDate(LocalDate.of(2000, 1, 1))
                .job(Job.STUDENT)
                .preferredCategory(Category.IT)
                .profileImage(file)
                .build());

        // 스터디 저장
        study = studyRepository.save(Study.builder()
                .maxMemberCount(5)
                .build());

        // 스터디 프로필 저장 (주소 포함)
        studyProfile = studyProfileRepository.save(StudyProfile.builder()
                .study(study)
                .title("백엔드 스터디")
                .bio("백엔드 개발자를 위한 스터디")
                .category(Category.IT)
                .address(new Address("경상북도", "경산시"))
                .build());

        // 스터디 멤버 (리더) 등록
        studyMemberRepository.save(StudyMember.builder()
                .study(study)
                .user(user)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());

        // 북마크 등록
        bookmarkRepository.save(Bookmark.builder()
                .study(study)
                .user(user)
                .build());

        // S3Uploader Mock - 항상 동일한 URL 리턴
        when(s3Uploader.getUrlFile(anyString()))
                .thenReturn("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile.png");
    }

    @AfterEach
    void tearDown() {
        bookmarkRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
        fileRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 홈 데이터 조회 API")
    void getHomeData_success() throws Exception {
        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.nickname").value("홍길동"))
                .andExpect(jsonPath("$.user.profileImageUrl")
                        .value("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile.png"))
                .andExpect(jsonPath("$.topStudies[0].title").value("백엔드 스터디"))
                .andExpect(jsonPath("$.topStudies[0].trustScore").value(80))
                .andExpect(jsonPath("$.topStudies[0].bookmarked").value(true));
    }

    @Test
    @DisplayName("실패: 프로필 없음 → 404 반환")
    void getHomeData_fail_profileNotFound() throws Exception {
        userProfileRepository.delete(profile);

        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("해당 유저의 프로필을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("성공: 프로필 이미지 없는 경우 → profileImageUrl 없음")
    void getHomeData_success_profileImageNull() throws Exception {
        profile = userProfileRepository.save(
                UserProfile.builder()
                        .id(profile.getId())
                        .user(user)
                        .nickname(profile.getNickname())
                        .address(profile.getAddress())
                        .birthDate(profile.getBirthDate())
                        .job(profile.getJob())
                        .preferredCategory(profile.getPreferredCategory())
                        .profileImage(null)
                        .build()
        );

        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.profileImageUrl").doesNotExist());
    }

    @Test
    @DisplayName("성공: 스터디 없는 경우 → topStudies = []")
    void getHomeData_success_noStudies() throws Exception {
        bookmarkRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();

        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topStudies").isEmpty());
    }

    @Test
    @DisplayName("성공: 스터디 10개 초과 → 상위 10개만 반환")
    void getHomeData_success_top10Only() throws Exception {
        IntStream.range(0, 15).forEach(i -> {
            User leader = userRepository.save(User.builder().trustScore(100L - i).build());
            Study newStudy = studyRepository.save(Study.builder().maxMemberCount(5).build());
            studyProfileRepository.save(StudyProfile.builder()
                    .study(newStudy)
                    .title("스터디" + i)
                    .bio("테스트 스터디")
                    .category(Category.IT)
                    .address(new Address("서울특별시", "강남구"))
                    .build());
            studyMemberRepository.save(StudyMember.builder()
                    .study(newStudy)
                    .user(leader)
                    .role(StudyRole.LEADER)
                    .status(StudyMemberStatus.JOINED)
                    .build());
        });

        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topStudies.length()").value(10));
    }

    @Test
    @DisplayName("성공: 북마크 없는 경우 → bookmarked=false")
    void getHomeData_success_notBookmarked() throws Exception {
        bookmarkRepository.deleteAll();

        mockMvc.perform(get("/api/home")
                        .with(user(new CustomUserDetails(user.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topStudies[0].bookmarked").value(false));
    }
}
