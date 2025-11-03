package com.study.focus.study;


import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.dto.GetStudyProfileResponse;
import com.study.focus.study.dto.StudyHomeResponse;
import com.study.focus.study.dto.UpdateStudyProfileRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.study.service.StudyService;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
    private UserProfileRepository userProfileRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private FileRepository fileRepository;


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
                "JPA 스터디", 10, List.of(Category.IT), "서울", "강남구", "JPA 심화 학습", "상세 설명"
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
    @Test
    @DisplayName("그룹 프로필 정보 조회 - 성공 (프로필 이미지 있음, File mock)")
    void getStudyProfile_Success_WithImage_MockFile() {
        // given
        Long studyId = 1L;
        Long userId = 2L;
        Long leaderId = 3L;
        String leaderNickname = "홍길동";
        String fileKey = "profile/leader.png";
        String expectedUrl = "https://cdn.example.com/profile/leader.png";

        Address address = Address.builder()
                .province("경상북도")
                .district("경산시")
                .build();
        Study study = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("알고리즘 스터디")
                .bio("매주 알고리즘 문제 풀이")
                .description("알고리즘 문제를 풀고 토론하는 스터디입니다.")
                .category(Category.IT)
                .address(address)
                .build();
        StudyMember leaderMember = StudyMember.builder().user(User.builder().id(leaderId).trustScore(82).build()).role(StudyRole.LEADER).build();
        User leader = leaderMember.getUser();
        File profileImage = mock(File.class);
        org.mockito.Mockito.when(profileImage.getFileKey()).thenReturn(fileKey);
        UserProfile leaderProfile = UserProfile.builder().user(leader).nickname(leaderNickname).profileImage(profileImage).build();

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyProfileRepository.findByStudy(study)).willReturn(Optional.of(profile));
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(userProfileRepository.findByUser(leader)).willReturn(Optional.of(leaderProfile));
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(7L);
        given(applicationRepository.findByApplicantIdAndStudyId(userId, studyId)).willReturn(Optional.empty());
        given(studyMemberRepository.existsByStudyIdAndUserIdAndStatus(studyId, userId, StudyMemberStatus.BANNED)).willReturn(false);
        given(s3Uploader.getUrlFile(fileKey)).willReturn(expectedUrl);

        // when
        GetStudyProfileResponse response = studyService.getStudyProfile(studyId, userId);

        // then
        assertThat(response.getId()).isEqualTo(studyId);
        assertThat(response.getTitle()).isEqualTo("알고리즘 스터디");
        assertThat(response.getLeader().getId()).isEqualTo(leaderId);
        assertThat(response.getLeader().getNickname()).isEqualTo(leaderNickname);
        assertThat(response.getLeader().getProfileImageUrl()).isEqualTo(expectedUrl);
        assertThat(response.getTrustScore()).isEqualTo(82);
        assertThat(response.getMemberCount()).isEqualTo(7);
        assertThat(response.getRecruitStatus()).isEqualTo(RecruitStatus.OPEN);
        assertThat(response.isCanApply()).isTrue();
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 성공 (프로필 이미지 없음)")
    void getStudyProfile_Success_NoImage() {
        // given
        Long studyId = 1L;
        Long userId = 2L;
        Long leaderId = 3L;
        String leaderNickname = "홍길동";

        Address address = Address.builder()
                .province("경상북도")
                .district("경산시")
                .build();
        Study study = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        StudyProfile profile = StudyProfile.builder()
                .study(study)
                .title("알고리즘 스터디")
                .bio("매주 알고리즘 문제 풀이")
                .description("알고리즘 문제를 풀고 토론하는 스터디입니다.")
                .category(Category.IT)
                .address(address)
                .build();
        StudyMember leaderMember = StudyMember.builder().user(User.builder().id(leaderId).trustScore(82).build()).role(StudyRole.LEADER).build();
        User leader = leaderMember.getUser();
        UserProfile leaderProfile = UserProfile.builder().user(leader).nickname(leaderNickname).profileImage(null).build();

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyProfileRepository.findByStudy(study)).willReturn(Optional.of(profile));
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(userProfileRepository.findByUser(leader)).willReturn(Optional.of(leaderProfile));
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(7L);
        given(applicationRepository.findByApplicantIdAndStudyId(userId, studyId)).willReturn(Optional.empty());
        given(studyMemberRepository.existsByStudyIdAndUserIdAndStatus(studyId, userId, StudyMemberStatus.BANNED)).willReturn(false);
        // 프로필 이미지가 없으므로 s3Uploader 호출 없음

        // when
        GetStudyProfileResponse response = studyService.getStudyProfile(studyId, userId);

        // then
        assertThat(response.getLeader().getProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 실패: 스터디 없음")
    void getStudyProfile_Fail_StudyNotFound() {
        // given
        Long studyId = 1L;
        Long userId = 2L;
        given(studyRepository.findById(studyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyService.getStudyProfile(studyId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("그룹 프로필 정보 조회 - 실패: 그룹장 프로필 없음")
    void getStudyProfile_Fail_LeaderProfileNotFound() {
        // given
        Long studyId = 1L;
        Long userId = 2L;
        Study study = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        Address address = Address.builder().province("경상북도").district("경산시").build();
        StudyProfile profile = StudyProfile.builder().study(study).address(address).build();
        StudyMember leaderMember = StudyMember.builder().user(User.builder().id(3L).build()).role(StudyRole.LEADER).build();
        User leader = leaderMember.getUser();

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyProfileRepository.findByStudy(study)).willReturn(Optional.of(profile));
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(userProfileRepository.findByUser(leader)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyService.getStudyProfile(studyId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("그룹 프로필 수정 - 성공")
    void updateStudyProfile_Success() {
        // given
        final Long studyId = 1L;
        final Long leaderUserId = 100L;
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "새로운 스터디 제목", 20, Category.IT, "경기도", "성남시", "새로운 한 줄 소개", "새로운 상세 설명"
        );

        User leaderUser = User.builder().id(leaderUserId).build();
        Study mockStudy = mock(Study.class); // study 객체도 Mock으로 만듭니다.
        StudyProfile mockStudyProfile = mock(StudyProfile.class); // studyProfile 객체도 Mock으로 만듭니다.
        StudyMember leaderMember = StudyMember.builder().user(leaderUser).study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(mockStudy));
        given(studyProfileRepository.findByStudy(mockStudy)).willReturn(Optional.of(mockStudyProfile));
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(5L);

        // when
        assertDoesNotThrow(() -> studyService.updateStudyProfile(studyId, leaderUserId, request));

        // then: 각 엔티티의 update 메서드가 정확히 1번씩 호출되었는지 검증
        // ArgumentCaptor를 사용하여 전달된 인자를 캡처
        ArgumentCaptor<Integer> maxMemberCaptor = ArgumentCaptor.forClass(Integer.class);
        then(mockStudy).should().updateMaxMemberCount(maxMemberCaptor.capture());
        assertThat(maxMemberCaptor.getValue()).isEqualTo(20);

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<String> bioCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        then(mockStudyProfile).should().update(
                titleCaptor.capture(), categoryCaptor.capture(), addressCaptor.capture(),
                bioCaptor.capture(), descriptionCaptor.capture()
        );
        assertThat(titleCaptor.getValue()).isEqualTo("새로운 스터디 제목");
        assertThat(categoryCaptor.getValue()).isEqualTo(Category.IT);
        assertThat(addressCaptor.getValue().getProvince()).isEqualTo("경기도");
        assertThat(bioCaptor.getValue()).isEqualTo("새로운 한 줄 소개");
    }

    @Test
    @DisplayName("그룹 프로필 수정 실패 - 방장이 아닌 경우")
    void updateStudyProfile_Fail_NotLeader() {
        // given: 방장이 아닌 사용자가 수정을 요청하는 상황
        final Long studyId = 1L;
        final Long notLeaderUserId = 999L; // 요청자 ID
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "제목", 10, Category.IT, "서울", "강남", "소개", "설명"
        );

        User actualLeaderUser = User.builder().id(100L).build(); // 실제 방장 ID
        StudyMember leaderMember = StudyMember.builder().user(actualLeaderUser).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then: 권한 없음 예외가 발생하는지 검증
        assertThatThrownBy(() -> studyService.updateStudyProfile(studyId, notLeaderUserId, request))
                .isInstanceOf(BusinessException.class);

        // DB 조회나 업데이트 관련 로직이 전혀 실행되지 않았는지 확인
        then(studyRepository).should(never()).findById(any());
        then(studyProfileRepository).should(never()).findByStudy(any());
    }

    @Test
    @DisplayName("그룹 프로필 수정 실패 - 최대 인원 수가 현재 인원보다 적은 경우")
    void updateStudyProfile_Fail_MaxMemberCountTooLow() {
        // given: 최대 인원 수를 현재 인원보다 적게 수정하려는 상황
        final Long studyId = 1L;
        final Long leaderUserId = 100L;
        final UpdateStudyProfileRequest request = new UpdateStudyProfileRequest(
                "제목", 5, Category.IT, "서울", "강남", "소개", "설명" // 5명으로 수정 요청
        );

        User leaderUser = User.builder().id(leaderUserId).build();
        Study mockStudy = mock(Study.class);
        StudyProfile mockStudyProfile = mock(StudyProfile.class);
        StudyMember leaderMember = StudyMember.builder().user(leaderUser).study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(mockStudy));
        given(studyProfileRepository.findByStudy(mockStudy)).willReturn(Optional.of(mockStudyProfile));
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(10L); // 현재 인원은 10명

        // when & then: 잘못된 요청 예외가 발생하는지 검증
        assertThatThrownBy(() -> studyService.updateStudyProfile(studyId, leaderUserId, request))
                .isInstanceOf(BusinessException.class); // 실제 예외 클래스에 맞게 수정

        // 엔티티의 update 관련 메서드가 호출되지 않았는지 확인
        then(mockStudy).should(never()).updateMaxMemberCount(any(Integer.class));
        then(mockStudyProfile).should(never()).update(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("스터디 메인 데이터 조회 - 성공")
    void getStudyHome_Success() {
        // given: 존재하는 studyId와 해당 스터디의 프로필을 설정
        final Long studyId = 1L;
        final String expectedTitle = "백엔드 스터디";

        StudyProfile mockProfile = StudyProfile.builder()
                .title(expectedTitle)
                .build();

        given(studyProfileRepository.findByStudyId(studyId)).willReturn(Optional.of(mockProfile));

        // when: 서비스 메서드 호출
        StudyHomeResponse response = studyService.getStudyHome(studyId);

        // then: 반환된 DTO가 null이 아니며, 기대한 title 값을 가지고 있는지 검증
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    @DisplayName("스터디 메인 데이터 조회 실패 - 존재하지 않는 스터디")
    void getStudyHome_Fail_StudyNotFound() {
        // given: 존재하지 않는 studyId를 설정
        final Long nonExistentStudyId = 999L;

        given(studyProfileRepository.findByStudyId(nonExistentStudyId)).willReturn(Optional.empty());

        // when & then: BusinessException이 발생하는지 검증
        assertThatThrownBy(() -> studyService.getStudyHome(nonExistentStudyId))
                .isInstanceOf(BusinessException.class);
    }

}