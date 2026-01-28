package com.planup.user.infrastructure.security;

import com.planup.user.domain.User;
import com.planup.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 1. 어떤 서비스인지 (kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // 2. PK가 되는 필드 이름 (sub, id 등)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 3. 속성값 정리 (OAuthAttributes Class 사용)
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 4. 로그인 성공 시 DB에 저장하거나 업데이트 (★핵심★)
        User user = saveOrUpdate(attributes);

        // 5. UserDetails로 반환 (SuccessHandler가 사용할 수 있도록)
        return new CustomUserDetails(user, attributes.getAttributes());
    }

    // 유저 저장 혹은 업데이트 로직
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getNickname(), attributes.getProfileImageUrl())) // 이미 있으면 정보 업데이트
                .orElse(attributes.toEntity()); // 없으면 새로 생성

        return userRepository.save(user); // DB 저장!
    }
}