package com.study.focus.account.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private Long userId;

    public CustomUserDetails(Long userId){
        this.userId = userId;
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    //앱 자체에 유저와 관리자 등 역할이 없어 임시 역할 부여(시큐리티가 인증을 확인하기 위해 권한 필드에 권한이 있는지 확인해야하기 때문에 반드시 필요)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections  .singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
