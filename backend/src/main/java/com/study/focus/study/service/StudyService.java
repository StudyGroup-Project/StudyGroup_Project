package com.study.focus.study.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {
    private final StudyRepository studyRepository;
    private final StudyProfileRepository studyProfileRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;

    // 스터디 그룹 생성
    public Long createStudy(Long userId, CreateStudyRequest createStudyRequest ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.URL_FORBIDDEN));
        // 스터디 생성
        Study study = Study.builder()
                .maxMemberCount(createStudyRequest.getMaxMemberCount())
                .recruitStatus(RecruitStatus.OPEN)
                .build();
        Study saveStudy = studyRepository.save(study);

        //스터디 프로필 생성
        Address address = Address.builder()
                .province(createStudyRequest.getProvince())
                .district(createStudyRequest.getDistrict())
                .build();

        StudyProfile studyProfile = StudyProfile.builder()
                .study(saveStudy)
                .title(createStudyRequest.getTitle())
                .bio(createStudyRequest.getBio())
                .description(createStudyRequest.getDescription())
                .address(address)
                .category(createStudyRequest.getCategory())
                .build();
        studyProfileRepository.save(studyProfile);


        StudyMember leader = StudyMember.builder()
                .study(saveStudy)
                .user(user)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();
        studyMemberRepository.save(leader);

        // 스터디의 ID 반환
        return saveStudy.getId();
    }

    // 그룹 프로필 정보 가져오기
    public void getStudyProfile(Long studyId) {
        // TODO: 스터디 프로필 조회
    }

    // 그룹 프로필 정보 수정하기
    public void updateStudyProfile(Long studyId) {
        // TODO: 스터디 프로필 수정
    }

    // 그룹 삭제
    public void deleteStudy(Long studyId) {
        // TODO: 스터디 삭제
    }

    // 스터디 그룹 검색 요청
    public void searchStudies() {
        // TODO: 스터디 검색
    }

    // 내 그룹 데이터 가져오기
    public void getMyStudies(Long userId) {
        // TODO: 내가 가입한 스터디 조회
    }
}