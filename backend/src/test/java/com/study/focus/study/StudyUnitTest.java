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


    @InjectMocks
    private StudyService studyService;

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
}