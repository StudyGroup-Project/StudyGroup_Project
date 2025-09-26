package com.study.focus.account.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 초기 프로필 설정
    public void createProfile(Long userId) {
        // TODO: 프로필 최초 생성
    }

    // 내 프로필 조회
    public void getMyProfile(Long userId) {
        // TODO: 내 프로필 조회
    }

    // 내 프로필 수정
    public void updateMyProfile(Long userId) {
        // TODO: 내 프로필 수정
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
