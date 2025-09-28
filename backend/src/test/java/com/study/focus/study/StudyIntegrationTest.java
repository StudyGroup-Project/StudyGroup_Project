package com.study.focus.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.Study;
import com.study.focus.study.dto.CreateStudyRequest;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyProfileRepository studyProfileRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
    }

    @AfterEach
    void tearDown() {
        bookmarkRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyProfileRepository.deleteAll();
        studyRepository.deleteAll();
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

}
