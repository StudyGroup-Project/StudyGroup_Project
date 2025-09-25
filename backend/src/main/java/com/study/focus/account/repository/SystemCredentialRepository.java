package com.study.focus.account.repository;

import com.study.focus.account.domain.SystemCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemCredentialRepository extends JpaRepository<SystemCredential, Long> {
}
