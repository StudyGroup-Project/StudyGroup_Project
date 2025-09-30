package com.study.focus.account.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Long userId;
        try {
            userId = Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new BusinessException(UserErrorCode.USER_ID_FORMAT_INVALID, e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return new CustomUserDetails(user.getId());
    }
}
