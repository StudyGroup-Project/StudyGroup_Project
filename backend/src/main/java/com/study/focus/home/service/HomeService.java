package com.study.focus.home.service;

import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.common.dto.StudyDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.home.dto.HomeResponse;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.domain.StudyProfile;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserProfileRepository userProfileRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final S3Uploader s3Uploader;

    @Transactional(readOnly = true)
    public HomeResponse getHomeData(Long userId) {
        // 1. 유저 프로필 조회
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_NOT_FOUND));

        String profileImageUrl = profile.getProfileImage() != null
                ? s3Uploader.getUrlFile(profile.getProfileImage().getFileKey())
                : null;

        // 2. 상위 스터디 조회 (방장 trustScore 내림차순 10개)
        List<StudyProfile> topProfiles =
                studyMemberRepository.findTop10StudyProfiles(PageRequest.of(0, 10));

        // 3. 유저가 찜한 스터디 ID 목록 조회
        Set<Long> bookmarkedStudyIds = bookmarkRepository.findAllByUserId(userId).stream()
                .map(b -> b.getStudy().getId())
                .collect(Collectors.toSet());

        // 4. DTO 변환
        List<StudyDto> studyDtos = topProfiles.stream()
                .map(sp -> {
                    Long studyId = sp.getStudy().getId();

                    int memberCount = (int) studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED);
                    long bookmarkCount = bookmarkRepository.countByStudyId(studyId);
                    long trustScore = studyMemberRepository.findLeaderTrustScoreByStudyId(studyId).orElse(0L);
                    boolean bookmarked = bookmarkedStudyIds.contains(studyId);

                    return StudyDto.of(sp, memberCount, bookmarkCount, trustScore, bookmarked);
                })
                .toList();

        return new HomeResponse(new HomeResponse.UserDto(profile, profileImageUrl), studyDtos);
    }
}