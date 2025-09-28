package com.study.focus.account.repository;

import com.study.focus.account.domain.SystemCredential;
import com.study.focus.account.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemCredentialRepository extends JpaRepository<SystemCredential, Long> {
    boolean existsByLoginId(String loginId);
    Optional<SystemCredential> findByLoginId(String loginId);
    Optional<SystemCredential> findByUser(User user);
}
