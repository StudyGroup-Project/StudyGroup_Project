package com.study.focus.account.repository;

import com.study.focus.account.domain.OAuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthCredentialRepository extends JpaRepository<OAuthCredential, Long> {
}
