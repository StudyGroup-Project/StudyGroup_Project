package com.study.focus.study.Member;

import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.service.UserService;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.service.GroupService;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.GetStudyMembersResponse;
import com.study.focus.study.dto.StudyMemberDto;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.service.StudyMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudyMemberUnitTest {

    @InjectMocks
    private StudyMemberService studyMemberService;

    @Mock
    private StudyMemberRepository studyMemberRepository;


    @InjectMocks
    private GroupService groupService;

    @Mock
    private UserService userService;

    private User user1;
    private User user2;

    private UserProfile profile1;
    private UserProfile profile2;

    private Study study1;

    private StudyMember member1;
    private StudyMember member2;


    @BeforeEach
    void setUp()
    {
        groupService = new GroupService(studyMemberRepository);
        studyMemberService = new StudyMemberService(studyMemberRepository,groupService,userService);
        user1 = User.builder().id(1L).trustScore(30)
                .lastLoginAt(LocalDateTime.now()).build();
        user2 = User.builder().id(2L).trustScore(40)
                .lastLoginAt(LocalDateTime.now()).build();

        profile1 = UserProfile.builder().id(1L)
                .user(user1).nickname("test")
                .build();

        profile2 = UserProfile.builder().id(2L)
                .user(user2).nickname("test")
                .build();


        study1 = Study.builder().id(1L).maxMemberCount(30)
                .recruitStatus(RecruitStatus.OPEN)
                .build();

        member1 = StudyMember.builder()
                .id(1L).user(user1).study(study1).role(StudyRole.MEMBER)
                .status(StudyMemberStatus.JOINED).build();


        member2 = StudyMember.builder()
                .id(2L).user(user2).study(study1).role(StudyRole.MEMBER)
                .status(StudyMemberStatus.JOINED).build();

    }

    @Test
    @DisplayName("스터디 멤버 목록 조회 : 멤버가 있는 경우 ")
    void getMembers_success() {
        // given
        Long studyId = 1L;
        Long userId = 1L;

        when(studyMemberRepository.findByStudyIdAndUserId(studyId,userId))
                .thenReturn(Optional.of(member1));

        when(studyMemberRepository.findAllByStudy_IdAndStatus(studyId, StudyMemberStatus.JOINED))
                .thenReturn(List.of(member1,member2));

        when(userService.getMyProfile(1L))
                .thenReturn(
                        new GetMyProfileResponse(
                                1L, profile1.getNickname()
                                ,null,null,
                                null,null,
                                null,"test1",
                                30L)
                );
        when(userService.getMyProfile(2L))
                .thenReturn(new GetMyProfileResponse(
                        2L, profile2.getNickname()
                        ,null,null,
                        null,null,
                        null,"test2",
                        30L));

        // when
        GetStudyMembersResponse response = studyMemberService.getMembers(studyId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStudyId()).isEqualTo(studyId);
        assertThat(response.getMembers()).hasSize(2);

        StudyMemberDto dto1 = response.getMembers().get(0);
        StudyMemberDto dto2 = response.getMembers().get(1);

        assertThat(dto1.getNickname()).isEqualTo(profile1.getNickname());
        assertThat(dto2.getNickname()).isEqualTo(profile2.getNickname());
        assertThat(dto1.getRole()).isEqualTo(StudyRole.MEMBER.name());
        assertThat(dto2.getRole()).isEqualTo(StudyRole.MEMBER.name());

        verify(studyMemberRepository, times(1))
                .findAllByStudy_IdAndStatus(studyId, StudyMemberStatus.JOINED);
        verify(userService, times(1)).getMyProfile(1L);
        verify(userService, times(1)).getMyProfile(2L);
    }
    

    @Test
    @DisplayName("스터디 멤버 목록 조회 실패 - 스터디 멤버가 아닌 경우")
    void getMembers_Fail_NotStudyMember() {
        // given
        Long studyId = 1L;
        Long nonMemberId = 999L;


        when(studyMemberRepository.findByStudyIdAndUserId(studyId, nonMemberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyMemberService.getMembers(studyId, nonMemberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_REQUEST);

        verify(studyMemberRepository, times(1))
                .findByStudyIdAndUserId(studyId, nonMemberId);
        verify(studyMemberRepository, never())
                .findAllByStudy_IdAndStatus(anyLong(), any());
    }








    @Test
    @DisplayName("그룹 인원 추방 - 성공")
    void expelMember_Success() {
        // given
        final Long studyId = 1L;
        final Long leaderId = 100L;
        final Long memberId = 200L;

        User leader = User.builder().id(leaderId).build();
        User memberToExpel = User.builder().id(memberId).build();
        Study study = Study.builder().id(studyId).build();

        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).status(StudyMemberStatus.JOINED).build();
        StudyMember targetMember = spy(StudyMember.builder().user(memberToExpel).study(study).role(StudyRole.MEMBER).status(StudyMemberStatus.JOINED).build());

        // [수정] 방장 권한 확인 시, 활동 중인(JOINED) 방장인지 확인
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER))
                .willReturn(Optional.of(leaderMember));
        // [수정] 추방 대상이 활동 중인(JOINED) 멤버인지 확인
        given(studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, memberId, StudyMemberStatus.JOINED))
                .willReturn(Optional.of(targetMember));

        // when
        studyMemberService.expelMember(studyId, memberId, leaderId);

        // then
        then(targetMember).should().updateStatus(StudyMemberStatus.BANNED);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 방장이 아닌 경우")
    void expelMember_Fail_NotLeader() {
        // given: 방장이 아닌 일반 멤버가 추방을 시도하는 상황
        final Long studyId = 1L;
        final Long leaderId = 100L;
        final Long notLeaderId = 300L;
        final Long memberId = 200L;

        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then: 권한 없음 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.expelMember(studyId, memberId, notLeaderId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.URL_FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 자기 자신을 추방하는 경우")
    void expelMember_Fail_SelfExpulsion() {
        // given: 방장이 자기 자신을 추방 대상으로 지정한 상황
        final Long studyId = 1L;
        final Long leaderId = 100L;

        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then: 잘못된 요청 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.expelMember(studyId, leaderId, leaderId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 추방 대상이 그룹 멤버가 아닌 경우")
    void expelMember_Fail_TargetNotMember() {
        // given: 추방 대상이 해당 스터디에 존재하지 않는 상황
        final Long studyId = 1L;
        final Long leaderId = 100L;
        final Long notMemberId = 404L;

        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, notMemberId, StudyMemberStatus.JOINED )).willReturn(Optional.empty());

        // when & then: 잘못된 파라미터 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.expelMember(studyId, notMemberId, leaderId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_PARAMETER);
    }

    // --- 그룹 탈퇴 테스트 ---

    @Test
    @DisplayName("그룹 탈퇴 - 성공")
    void leaveStudy_Success() {
        // given: 일반 멤버가 탈퇴를 요청하는 정상적인 상황
        final Long studyId = 1L;
        final Long memberId = 200L;

        User member = User.builder().id(memberId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember targetMember = spy(StudyMember.builder().user(member).study(study).role(StudyRole.MEMBER).build());

        given(studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, memberId,StudyMemberStatus.JOINED)).willReturn(Optional.of(targetMember));

        // when
        studyMemberService.leaveStudy(studyId, memberId);

        // then: 대상 멤버의 상태가 'LEFT'로 변경되는지 검증
        then(targetMember).should().updateStatus(StudyMemberStatus.LEFT);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 방장이 탈퇴를 시도하는 경우")
    void leaveStudy_Fail_LeaderCannotLeave() {
        // given: 방장이 탈퇴를 시도하는 상황
        final Long studyId = 1L;
        final Long leaderId = 100L;

        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, leaderId, StudyMemberStatus.JOINED)).willReturn(Optional.of(leaderMember));

        // when & then: 잘못된 요청 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.leaveStudy(studyId, leaderId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 멤버가 아닌 사용자가 탈퇴를 시도하는 경우")
    void leaveStudy_Fail_NotAMember() {
        // given: 스터디 멤버가 아닌 사용자가 탈퇴를 시도하는 상황
        final Long studyId = 1L;
        final Long notMemberId = 999L;

        given(studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, notMemberId, StudyMemberStatus.JOINED)).willReturn(Optional.empty());

        // when & then: 잘못된 파라미터 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.leaveStudy(studyId, notMemberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_PARAMETER);
    }

}
