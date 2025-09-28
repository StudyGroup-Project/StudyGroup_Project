package com.study.focus.account.service;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.dto.InitUserProfileRequest;
import com.study.focus.account.dto.UpdateUserProfileRequest;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
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

    public void initProfile(Long userId, InitUserProfileRequest request) {
        User user = findUser(userId);

        Address address = new Address(request.getProvince(), request.getDistrict());
        UserProfile profile = UserProfile.create(
                user,
                request.getNickname(),
                address,
                LocalDate.parse(request.getBirthDate()),
                request.getJob(),
                request.getPreferredCategory()
        );

        userProfileRepository.save(profile);
    }

    public String setProfileImage(Long userId, MultipartFile file) {
        UserProfile profile = findUserProfile(userId);

        FileDetailDto meta = s3Uploader.makeMetaData(file);

        try {
            s3Uploader.uploadFile(meta.getKey(), file);
        } catch (Exception e) {
            throw new BusinessException(UserErrorCode.FILE_UPLOAD_FAIL, e);
        }

        File newFile = File.ofProfileImage(meta);
        profile.updateProfileImage(newFile);

        return s3Uploader.getUrlFile(meta.getKey());
    }

    public void updateProfile(Long userId, UpdateUserProfileRequest request) {
        UserProfile profile = findUserProfile(userId);

        Address newAddress = new Address(request.getProvince(), request.getDistrict());
        profile.updateProfile(
                request.getNickname(),
                newAddress,
                LocalDate.parse(request.getBirthDate()),
                request.getJob(),
                request.getPreferredCategory()
        );
    }

    @Transactional(readOnly = true)
    public GetMyProfileResponse getMyProfile(Long userId) {
        UserProfile profile = findUserProfile(userId);

        return new GetMyProfileResponse(
                profile.getUser().getId(),
                profile.getNickname(),
                profile.getAddress().getProvince(),
                profile.getAddress().getDistrict(),
                profile.getBirthDate().toString(),
                profile.getJob(),
                profile.getPreferredCategory(),
                profile.getProfileImage() != null ? s3Uploader.getUrlFile(profile.getProfileImage().getFileKey()) : null,
                profile.getUser().getTrustScore()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private UserProfile findUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_NOT_FOUND));
    }
}
