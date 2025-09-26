package com.study.focus.account.repository;

import com.study.focus.account.domain.OAuthCredential;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthCredentialRepository extends JpaRepository<OAuthCredential, Long> {
    Optional<OAuthCredential> findByProviderAndProviderUserId(Provider provider, String providerUserId);

    Optional<OAuthCredential> findByUser(User user);
}
