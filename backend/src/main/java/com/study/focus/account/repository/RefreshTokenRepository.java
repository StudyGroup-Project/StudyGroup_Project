package com.study.focus.account.repository;

import com.study.focus.account.domain.RefreshToken;
import com.study.focus.account.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUser(User user);
    void deleteByUserId(Long userId);
}
