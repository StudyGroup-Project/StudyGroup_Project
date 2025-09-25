package com.study.focus.account.config.oauth;

import com.study.focus.account.domain.LoginType;
import com.study.focus.account.domain.OAuthCredential;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.account.service.RefreshTokenService;
import com.study.focus.account.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao, naver
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        System.out.println("registrationId = " + registrationId);
        System.out.println("provider = " + provider);

        // providerId 추출 (각 provider별로 다름)
        String providerUserId = extractProviderId(provider, oAuth2User.getAttributes());

        // Credential 확인
        OAuthCredential credential = oAuthCredentialRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> {
                    // 신규 사용자 → User 생성
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

        // 여기서는 단순히 UserId만 CustomOAuth2User로 넘김
        return new CustomOAuth2User(user.getId(), oAuth2User.getAttributes(), null);
    }

    private String extractProviderId(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case KAKAO -> String.valueOf(attributes.get("id")); // Long → String 변환
            case NAVER -> ((Map<String, Object>) attributes.get("response")).get("id").toString();
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth Provider입니다: " + provider);
        };
    }
}
