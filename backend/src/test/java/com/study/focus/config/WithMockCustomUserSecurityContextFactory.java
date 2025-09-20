package com.study.focus.config;

import com.study.focus.account.dto.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<MockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(MockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails customUserDetails = new CustomUserDetails(customUser.userId());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(customUserDetails,
                "password",customUserDetails.getAuthorities());

        context.setAuthentication(token);
        return context ;
    }
}
