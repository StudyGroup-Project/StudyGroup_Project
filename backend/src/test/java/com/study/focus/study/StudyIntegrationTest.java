package com.study.focus.study;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.dto.GetStudyProfileResponse;
import com.study.focus.study.dto.UpdateStudyProfileRequest;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyProfileRepository studyProfileRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private FileRepository fileRepository;


    @MockitoBean
    private S3Uploader s3Uploader;

    private User testUser;
    private User leader;
    private Study testStudy;
    private StudyProfile testProfile;
    private StudyMember leaderMember;
    private UserProfile leaderProfile;


    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
        leader = userRepository.save(User.builder().trustScore(82).build());
        testStudy = studyRepository.save(Study.builder()
                .recruitStatus(RecruitStatus.OPEN)
                .maxMemberCount(10)
                .build());
        testProfile = studyProfileRepository.save(StudyProfile.builder()
                .study(testStudy) // 반드시 save()로 영속화된 객체
                .title("알고리즘 스터디")
                .bio("매주 알고리즘 문제 풀이")
                .description("알고리즘 문제를 풀고 토론하는 스터디입니다.")
                .category(Category.IT)
                .address(Address.builder().province("경상북도").district("경산시").build())
                .build());
        leaderMember = studyMemberRepository.save(StudyMember.builder()
                .study(testStudy)
                .user(leader)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build());
        Address address = Address.builder().province("서울특별시").district("강남구").build();
        System.out.println("address.district = " + address.getDistrict());
        leaderProfile = userProfileRepository.save(UserProfile.builder()
                .user(leader)
                .nickname("홍길동")
                .address(address)
                .birthDate(LocalDate.of(2000, 1, 1))
                .job(Job.STUDENT)
                .preferredCategory(Category.IT)
                .profileImage(null)
                .build());
    }

    @AfterEach
    void tearDown() {
        applicationRepository.deleteAll();
        bookmarkRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("스터디 그룹 생성 - 성공")
    void createStudy_Success() throws Exception {
        CreateStudyRequest request = new CreateStudyRequest(
                "통합 테스트 스터디", 10, Category.IT, "서울", "마포구", "API 통합 테스트", "상세 설명"
        );

        long initialStudyCount = studyRepository.count();

        mockMvc.perform(post("/api/studies")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertThat(studyRepository.count()).isEqualTo(initialStudyCount + 1);
    }

    @Test
    @DisplayName("스터디 그룹 생성 실패 - 존재하지 않는 사용자의 요청")
    void createStudy_Fail_UserNotFound() throws Exception {

        CreateStudyRequest request = new CreateStudyRequest(
                "실패할 스터디", 10, Category.IT, "경기", "성남시", "소개", "설명"
        );
        long nonExistentUserId = 999L;

        long initialStudyCount = studyRepository.count();


        mockMvc.perform(post("/api/studies")
                        .with(user(new CustomUserDetails(nonExistentUserId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(studyRepository.count()).isEqualTo(initialStudyCount);
    }


    @Test
    @DisplayName("스터디 그룹 찜하기 - 성공")
    void addBookmark_Success() throws Exception {
        // given (상황 설정)
        // 테스트를 위한 스터디를 미리 하나 생성
        Study testStudy = studyRepository.save(Study.builder().build());
        long initialBookmarkCount = bookmarkRepository.count();

        // when & then (실행 및 검증)
        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId()))) // testUser로 로그인
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk()); // 200 OK 상태를 기대

        // DB에 북마크가 실제로 1개 늘었는지 확인
        assertThat(bookmarkRepository.count()).isEqualTo(initialBookmarkCount + 1);
    }

    @Test
    @DisplayName("스터디 그룹 찜하기 실패 - 이미 찜한 경우")
    void addBookmark_Fail_AlreadyExists() throws Exception {
        // given
        Study testStudy = studyRepository.save(Study.builder().build());
        // 테스트 전에 미리 한 번 찜하기를 실행해서 '이미 찜한 상태'를 만듦
        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isOk());

        long initialBookmarkCount = bookmarkRepository.count();

        // when & then
        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertThat(bookmarkRepository.count()).isEqualTo(initialBookmarkCount);
    }

    @Test
    @DisplayName("스터디 그룹 찜 해제 - 성공")
    void removeBookmark_Success() throws Exception {
        // given (상황 설정)
        Study testStudy = studyRepository.save(Study.builder().build());

        // 찜 해제를 테스트하려면, 먼저 찜이 되어있어야 함
        // POST API를 호출하여 '이미 찜한 상태'를 미리 만듦
        mockMvc.perform(post("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isOk());

        // 찜이 실제로 생성되었는지 확인 (현재 북마크 개수: 1)
        long initialBookmarkCount = bookmarkRepository.count();
        assertThat(initialBookmarkCount).isEqualTo(1);

        // when & then (실행 및 검증)
        // 찜 해제 API(DELETE)를 호출
        mockMvc.perform(delete("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent()); // 성공적으로 삭제되었으므로 204 No Content를 기대

        // DB에서 북마크가 실제로 1개 줄었는지 확인
        assertThat(bookmarkRepository.count()).isEqualTo(initialBookmarkCount - 1);
        assertThat(bookmarkRepository.count()).isZero(); // 최종적으로 0개가 되어야 함
    }

    @Test
    @DisplayName("스터디 그룹 찜 해제 실패 - 찜한 기록이 없는 경우")
    void removeBookmark_Fail_BookmarkNotFound() throws Exception {
        // given
        Study testStudy = studyRepository.save(Study.builder().build());

        // 찜한 기록이 없는 상태에서 시작
        long initialBookmarkCount = bookmarkRepository.count();
        assertThat(initialBookmarkCount).isZero();

        // when & then
        // 찜한 적 없는 스터디에 대해 찜 해제 API(DELETE)를 호출
        mockMvc.perform(delete("/api/studies/" + testStudy.getId() + "/bookmark")
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                // 서비스에서 BusinessException이 발생하고, 400 Bad Request로 처리될 것을 기대
                .andExpect(status().isBadRequest());

        // 북마크 개수가 변하지 않았는지 확인
        assertThat(bookmarkRepository.count()).isEqualTo(initialBookmarkCount);
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 성공 (프로필 이미지 없음)")
    void getStudyProfile_Success_NoImage() throws Exception {
        mockMvc.perform(get("/api/studies/" + testStudy.getId())
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    GetStudyProfileResponse response = objectMapper.readValue(json, GetStudyProfileResponse.class);
                    assertThat(response.getId()).isEqualTo(testStudy.getId());
                    assertThat(response.getLeader().getNickname()).isEqualTo("홍길동");
                    assertThat(response.getLeader().getProfileImageUrl()).isNull();
                    assertThat(response.isCanApply()).isTrue();
                });
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 성공 (프로필 이미지 있음)")
    void getStudyProfile_Success_WithImage() throws Exception {
        String fileKey = "profile/leader.png";
        String expectedUrl = "http://localhost/test-bucket/" + fileKey;
        FileDetailDto fileDetail = new FileDetailDto("profile.png", fileKey, "image/png", 100L);
        File profileImage = File.ofResource(null, fileDetail); // 관계 없는 필드는 null

        fileRepository.save(profileImage);
        // 이미 저장된 leaderProfile의 필드만 변경
        leaderProfile.updateProfileImage(profileImage); // profileImage만 변경
        userProfileRepository.save(leaderProfile); // update 쿼리로 동작
        org.mockito.Mockito.when(s3Uploader.getUrlFile(fileKey)).thenReturn(expectedUrl);

        mockMvc.perform(get("/api/studies/" + testStudy.getId())
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    GetStudyProfileResponse response = objectMapper.readValue(json, GetStudyProfileResponse.class);
                    assertThat(response.getLeader().getProfileImageUrl()).isEqualTo(expectedUrl);
                });
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 실패: 스터디 없음")
    void getStudyProfile_Fail_StudyNotFound() throws Exception {
        long nonExistentStudyId = 999L;
        mockMvc.perform(get("/api/studies/" + nonExistentStudyId)
                        .with(user(new CustomUserDetails(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹 프로필 정보 수정 - 성공")
    void updateStudyProfile_Success() throws Exception {
        // given: 방장이 프로필 수정을 요청할 데이터
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "JPA 마스터 스터디", // title
                20,                 // maxMemberCount
                Category.DESIGN,    // category
                "서울특별시",         // province
                "강남구",             // district
                "JPA 전문가가 되기 위한 스터디", // bio
                "더 심도 있는 학습을 진행합니다." // description
        );

        // when: 방장 권한으로 PUT 요청
        mockMvc.perform(put("/api/studies/{studyId}", testStudy.getId())
                        .with(user(new CustomUserDetails(leader.getId()))) // 방장으로 인증
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // then: 200 OK 응답을 기대

        // then: DB에서 직접 데이터를 다시 조회하여 변경 사항을 검증
        Study updatedStudy = studyRepository.findById(testStudy.getId()).orElseThrow();
        StudyProfile updatedProfile = studyProfileRepository.findByStudy(testStudy).orElseThrow();

        assertThat(updatedStudy.getMaxMemberCount()).isEqualTo(request.getMaxMemberCount());
        assertThat(updatedProfile.getTitle()).isEqualTo(request.getTitle());
        assertThat(updatedProfile.getCategory()).isEqualTo(request.getCategory());
        assertThat(updatedProfile.getAddress().getProvince()).isEqualTo(request.getProvince());
        assertThat(updatedProfile.getBio()).isEqualTo(request.getBio());
        assertThat(updatedProfile.getDescription()).isEqualTo(request.getDescription());
    }

    @Test
    @DisplayName("그룹 프로필 정보 수정 실패 - 방장이 아닌 경우")
    void updateStudyProfile_Fail_NotLeader() throws Exception {
        // given: 방장이 아닌 일반 사용자와 요청 데이터
        final User notLeader = userRepository.save(User.builder().build());
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "수정 시도", 10, Category.IT, "서울", "강남", "소개", "설명"
        );

        // when: 방장이 아닌 사용자로 PUT 요청
        mockMvc.perform(put("/api/studies/" + testStudy.getId())
                        .with(user(new CustomUserDetails(notLeader.getId()))) // 방장이 아닌 사용자로 인증
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // then: 403 Forbidden 응답을 기대
    }

    @Test
    @DisplayName("그룹 프로필 정보 수정 실패 - 최대 인원 수가 현재 인원보다 적은 경우")
    void updateStudyProfile_Fail_MaxMemberCountTooLow() throws Exception {
        // given: 현재 멤버(leader)는 1명. 최대 인원을 0으로 수정 요청
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "수정 시도", 0, Category.IT, "서울", "강남", "소개", "설명"
        );

        // when: 방장 권한으로 PUT 요청
        mockMvc.perform(put("/api/studies/" + testStudy.getId())
                        .with(user(new CustomUserDetails(leader.getId()))) // 방장으로 인증
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // then: 400 Bad Request 응답을 기대
    }

    @Test
    @DisplayName("스터디 메인 데이터 조회 - 성공")
    void getStudyHome_Success() throws Exception {
        // given: setUp()에서 'testStudy'와 'testProfile'이 생성되었음
        // testProfile의 title은 "알고리즘 스터디"

        // when & then: 생성된 testStudy의 ID로 API를 호출하고 결과를 검증
        mockMvc.perform(get("/api/studies/{studyId}/home", testStudy.getId())
                        .with(user(new CustomUserDetails(leader.getId())))) // 스터디 멤버로 인증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("알고리즘 스터디"));
    }

    @Test
    @DisplayName("스터디 메인 데이터 조회 실패 - 존재하지 않는 스터디")
    void getStudyHome_Fail_StudyNotFound() throws Exception {
        // given: 존재하지 않는 스터디 ID
        final long nonExistentStudyId = 999L;

        // when & then: 존재하지 않는 ID로 API를 호출하면 400 Bad Request가 반환되는지 검증
        mockMvc.perform(get("/api/studies/{studyId}/home", nonExistentStudyId)
                        .with(user(new CustomUserDetails(leader.getId()))))
                .andExpect(status().isBadRequest());
    }

}
