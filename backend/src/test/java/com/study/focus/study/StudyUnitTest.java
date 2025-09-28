package com.study.focus.study;


import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Category;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyProfile;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.study.service.StudyService;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import com.study.focus.study.domain.Bookmark;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.service.BookmarkService;

@ExtendWith(MockitoExtension.class)
class StudyUnitTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyProfileRepository studyProfileRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private StudyService studyService;

    @InjectMocks
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("스터디 그룹 생성 - 성공")
    void createStudy_Success() {
        final Long userId = 1L;
        final Long expectedStudyId = 10L;
        CreateStudyRequest request = new CreateStudyRequest(
                "JPA 스터디", 10, Category.IT, "서울", "강남구", "JPA 심화 학습", "상세 설명"
        );

        User fakeUser = User.builder().id(userId).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));


        Study fakeStudy = Study.builder().id(expectedStudyId).build();
        given(studyRepository.save(any(Study.class))).willReturn(fakeStudy);


        Long actualStudyId = studyService.createStudy(userId, request);

        assertThat(actualStudyId).isEqualTo(expectedStudyId);

        then(studyRepository).should().save(any(Study.class));
        then(studyProfileRepository).should().save(any(StudyProfile.class));
        then(studyMemberRepository).should().save(any(StudyMember.class));
    }

    @Test
    @DisplayName("스터디 그룹 생성 실패 - 존재하지 않는 유저")
    void createStudy_Fail_UserNotFound() {
        final Long nonExistentUserId = 999L;
        CreateStudyRequest request = new CreateStudyRequest(
                "JPA 스터디", 10, Category.IT, "서울", "강남구", "JPA 심화 학습", "상세 설명"
        );

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studyService.createStudy(nonExistentUserId, request))
                .isInstanceOf(BusinessException.class);

        then(studyRepository).should(never()).save(any());
        then(studyProfileRepository).should(never()).save(any());
        then(studyMemberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("스터디 찜하기 - 성공")
    void addBookmark_Success() {
        // given (상황 설정)
        final Long userId = 1L;
        final Long studyId = 10L;

        User fakeUser = User.builder().id(userId).build();
        Study fakeStudy = Study.builder().id(studyId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        // 아직 찜하지 않은 상태이므로, findByUserAndStudy는 비어있는 Optional을 반환하도록 설정
        given(bookmarkRepository.findByUserAndStudy(fakeUser, fakeStudy)).willReturn(Optional.empty());

        // when
        // 예외가 발생하지 않아야 함
        assertThatCode(() -> bookmarkService.addBookmark(userId, studyId))
                .doesNotThrowAnyException();

        // then (결과 검증)
        // bookmarkRepository의 save 메서드가 한 번 호출되었는지 검증
        then(bookmarkRepository).should().save(any(Bookmark.class));
    }

    @Test
    @DisplayName("스터디 찜하기 실패 - 이미 찜한 경우")
    void addBookmark_Fail_AlreadyExists() {
        // given
        final Long userId = 1L;
        final Long studyId = 10L;

        User fakeUser = User.builder().id(userId).build();
        Study fakeStudy = Study.builder().id(studyId).build();
        Bookmark fakeBookmark = Bookmark.builder().build(); // 이미 존재하는 북마크

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        // 이미 찜한 상태
        given(bookmarkRepository.findByUserAndStudy(fakeUser, fakeStudy)).willReturn(Optional.of(fakeBookmark));

        // when & then

        assertThatThrownBy(() -> bookmarkService.addBookmark(userId, studyId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("잘못된 요청입니다"); // 에러 메시지 검증 (실제 메시지에 맞게 수정)


        then(bookmarkRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("스터디 찜하기 실패 - 존재하지 않는 유저")
    void addBookmark_Fail_UserNotFound() {
        // given
        final Long nonExistentUserId = 999L;
        final Long studyId = 10L;

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(nonExistentUserId, studyId))
                .isInstanceOf(BusinessException.class);

        // save 메서드가 절대 호출되지 않았는지 검증
        then(bookmarkRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("스터디 찜하기 실패 - 존재하지 않는 스터디")
    void addBookmark_Fail_StudyNotFound() {
        // given
        final Long userId = 1L;
        final Long nonExistentStudyId = 999L;

        User fakeUser = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(nonExistentStudyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(userId, nonExistentStudyId))
                .isInstanceOf(BusinessException.class);

        // save 메서드가 절대 호출되지 않았는지 검증
        then(bookmarkRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("스터디 찜 해제 - 성공")
    void removeBookmark_Success() {
        // given (상황 설정)
        final Long userId = 1L;
        final Long studyId = 10L;

        User fakeUser = User.builder().id(userId).build();
        Study fakeStudy = Study.builder().id(studyId).build();
        Bookmark fakeBookmark = Bookmark.builder().id(100L).build(); // 삭제될 북마크 객체

        // 사용자와 스터디가 모두 존재
        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        // 삭제할 북마크가 DB에 존재
        given(bookmarkRepository.findByUserAndStudy(fakeUser, fakeStudy)).willReturn(Optional.of(fakeBookmark));

        // when
        assertThatCode(() -> bookmarkService.removeBookmark(userId, studyId))
                .doesNotThrowAnyException();

        // then
        then(bookmarkRepository).should().delete(fakeBookmark);
    }

    @Test
    @DisplayName("스터디 찜 해제 실패 - 찜한 기록이 없는 경우")
    void removeBookmark_Fail_BookmarkNotFound() {
        // given
        final Long userId = 1L;
        final Long studyId = 10L;

        User fakeUser = User.builder().id(userId).build();
        Study fakeStudy = Study.builder().id(studyId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));

        given(bookmarkRepository.findByUserAndStudy(fakeUser, fakeStudy)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.removeBookmark(userId, studyId))
                .isInstanceOf(BusinessException.class);


        then(bookmarkRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("스터디 찜 해제 실패 - 존재하지 않는 사용자의 요청")
    void removeBookmark_Fail_UserNotFound() {
        // given
        final Long nonExistentUserId = 999L;
        final Long studyId = 10L;

        // 존재하지 않는 사용자로 설정
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.removeBookmark(nonExistentUserId, studyId))
                .isInstanceOf(BusinessException.class);

        then(bookmarkRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("스터디 찜 해제 실패 - 존재하지 않는 스터디")
    void removeBookmark_Fail_StudyNotFound() {
        // given
        final Long userId = 1L;
        final Long nonExistentStudyId = 999L;

        User fakeUser = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        // 존재하지 않는 스터디로 설정
        given(studyRepository.findById(nonExistentStudyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookmarkService.removeBookmark(userId, nonExistentStudyId))
                .isInstanceOf(BusinessException.class);

        then(bookmarkRepository).should(never()).delete(any());
    }

}