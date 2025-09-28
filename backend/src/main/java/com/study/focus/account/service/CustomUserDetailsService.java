package com.study.focus.account.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // DB 접근 Repository (가정)

    /**
     * UserDetailsService 인터페이스의 핵심 메서드입니다.
     * Spring Security 필터 체인이나, 저희가 만든 TokenAuthenticationFilter에서 호출됩니다.
     *
     * @param username 여기서는 JWT의 Subject로 사용한 userId 문자열이 전달됩니다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. JWT Subject (userId)는 String으로 넘어오므로 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user ID format: " + username);
        }

        // 2. userId로 DB에서 사용자 정보를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // 3. 조회된 Account 객체를 CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(user.getId());
    }
}
