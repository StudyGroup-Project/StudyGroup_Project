package com.study.focus.study.Member;

import com.study.focus.account.domain.User;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.service.StudyMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class StudyMemberUnitTest {

    @InjectMocks
    private StudyMemberService studyMemberService;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Test
    @DisplayName("그룹 인원 추방 - 성공")
    void expelMember_Success() {
        // given: 방장이 멤버를 추방하는 정상적인 상황
        final Long studyId = 1L;
        final Long leaderId = 100L;
        final Long memberId = 200L;

        User leader = User.builder().id(leaderId).build();
        User memberToExpel = User.builder().id(memberId).build();
        Study study = Study.builder().id(studyId).build();

        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();
        StudyMember targetMember = StudyMember.builder().user(memberToExpel).study(study).role(StudyRole.MEMBER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, memberId)).willReturn(Optional.of(targetMember));

        // when: 서비스 메서드 호출
        assertThatCode(() -> studyMemberService.expelMember(studyId, memberId, leaderId))
                .doesNotThrowAnyException();

        // then: delete 메서드가 정확히 한 번 호출되었는지 검증
        then(studyMemberRepository).should().delete(targetMember);
    }

    @Test
    @DisplayName("그룹 인원 추방 실패 - 방장이 아닌 경우")
    void expelMember_Fail_NotLeader() {
        // given: 방장이 아닌 일반 멤버가 추방을 시도하는 상황
        final Long studyId = 1L;
        final Long notLeaderId = 300L; // 요청자
        final Long memberId = 200L;   // 추방 대상

        User leader = User.builder().id(100L).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then: 권한 없음 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.expelMember(studyId, memberId, notLeaderId))
                .isInstanceOf(BusinessException.class);

        // delete 메서드가 호출되지 않았는지 확인
        then(studyMemberRepository).should(never()).delete(any(StudyMember.class));
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
                .isInstanceOf(BusinessException.class);

        then(studyMemberRepository).should(never()).delete(any(StudyMember.class));
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
        // findByStudyIdAndUserId 호출 시, 비어있는 Optional을 반환하도록 설정
        given(studyMemberRepository.findByStudyIdAndUserId(studyId, notMemberId)).willReturn(Optional.empty());

        // when & then: 잘못된 파라미터 예외(BusinessException)가 발생하는지 검증
        assertThatThrownBy(() -> studyMemberService.expelMember(studyId, notMemberId, leaderId))
                .isInstanceOf(BusinessException.class);

        then(studyMemberRepository).should(never()).delete(any(StudyMember.class));
    }

}
