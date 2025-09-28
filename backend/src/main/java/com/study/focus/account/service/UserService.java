package com.study.focus.account.service;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.dto.InitProfileRequest;
import com.study.focus.account.dto.UpdateUserProfileRequest;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final UserProfileRepository userProfileRepository;

    /**
     * 초기 프로필 설정 (이미지 제외)
     */
    public void initProfile(Long userId, InitProfileRequest request) {
        User user = findUser(userId);

        Address address = new Address(request.getProvince(), request.getDistrict());
        UserProfile profile = UserProfile.create(
                user,
                request.getNickname(),
                address,
                LocalDate.parse(request.getBirthDate()),
                Job.valueOf(request.getJob()),            // enum 매핑
                Category.valueOf(request.getPreferredCategory())
        );

        userProfileRepository.save(profile);
    }

    /**
     * 프로필 이미지 등록 / 변경
     */
    public String setProfileImage(Long userId, MultipartFile file) {
        UserProfile profile = findUserProfile(userId);

        // 파일 메타데이터 생성
        FileDetailDto meta = s3Uploader.makeMetaData(file);

        // 업로드
        try {
            s3Uploader.uploadFile(meta.getKey(), file);
        } catch (Exception e) {
            throw new BusinessException(UserErrorCode.FILE_UPLOAD_FAIL, e);
        }

        // File 엔티티 생성 후 교체
        File newFile = File.ofProfileImage(meta);
        profile.updateProfileImage(newFile);

        return s3Uploader.getUrlFile(meta.getKey());
    }

    /**
     * 프로필 수정 (이미지 제외)
     */
    public void updateProfile(Long userId, UpdateUserProfileRequest request) {
        UserProfile profile = findUserProfile(userId);

        Address newAddress = new Address(request.getProvince(), request.getDistrict());
        profile.updateProfile(
                request.getNickname(),
                newAddress,
                LocalDate.parse(request.getBirthDate()),
                Job.valueOf(request.getJob()),
                Category.valueOf(request.getPreferredCategory())
        );
    }

    /**
     * 내 프로필 조회
     */
    @Transactional(readOnly = true)
    public GetMyProfileResponse getMyProfile(Long userId) {
        UserProfile profile = findUserProfile(userId);

        return new GetMyProfileResponse(
                profile.getUser().getId(),
                profile.getNickname(),
                profile.getAddress().getProvince(),
                profile.getAddress().getDistrict(),
                profile.getBirthDate().toString(),
                profile.getJob().name(),
                profile.getPreferredCategory().name(),
                profile.getProfileImage() != null ? s3Uploader.getUrlFile(profile.getProfileImage().getFileKey()) : null,
                profile.getUser().getTrustScore()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }

    private UserProfile findUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.FILE_NOT_FOUND));
    }
}
