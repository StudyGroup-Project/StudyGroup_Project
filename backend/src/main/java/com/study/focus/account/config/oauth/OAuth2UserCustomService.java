package com.study.focus.account.config.oauth;

import com.study.focus.account.domain.LoginType;
import com.study.focus.account.domain.OAuthCredential;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthCredentialRepository oAuthCredentialRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        String providerUserId = extractProviderId(provider, oAuth2User.getAttributes());

        OAuthCredential credential = oAuthCredentialRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .loginType(LoginType.OAUTH)
                            .build();
                    userRepository.save(newUser);

                    OAuthCredential newCredential = OAuthCredential.builder()
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .user(newUser)
                            .build();
                    return oAuthCredentialRepository.save(newCredential);
                });

        User user = credential.getUser();
        user.updateLastLoginAt();

        return new CustomOAuth2User(user.getId(), oAuth2User.getAttributes(), null);
    }

    private String extractProviderId(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case KAKAO -> String.valueOf(attributes.get("id"));
            case NAVER -> ((Map<String, Object>) attributes.get("response")).get("id").toString();
            default -> throw new BusinessException(UserErrorCode.UNSUPPORTED_PROVIDER);
        };
    }
}
