package com.tjg_project.candy.domain.auth.service;

import com.tjg_project.candy.domain.user.entity.Users;
import com.tjg_project.candy.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class KakaoAppLoginService {
    private static final Logger log = LoggerFactory.getLogger(KakaoAppLoginService.class);
    private static final String PROVIDER = "kakao";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public KakaoAppLoginService(
            RestTemplate restTemplate,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResult login(String kakaoAccessToken) {
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new IllegalArgumentException("Kakao accessToken is required.");
        }

        KakaoProfile kakaoProfile = fetchKakaoProfile(kakaoAccessToken);
        String socialUserId = PROVIDER + "_" + kakaoProfile.id();

        log.info(
                "Kakao app login user info: kakaoId={}, nickname={}, email={}",
                kakaoProfile.id(),
                kakaoProfile.nickname(),
                kakaoProfile.email()
        );

        return userRepository.findByUserIdAndProvider(socialUserId, PROVIDER)
                .map(user -> new LoginResult(updateSocialUser(user, kakaoProfile), false))
                .orElseGet(() -> userRepository.findByEmail(kakaoProfile.email())
                        .map(user -> new LoginResult(updateExistingEmailUser(user, kakaoProfile), false))
                        .orElseGet(() -> new LoginResult(createSocialUser(socialUserId, kakaoProfile), true)));
    }

    private KakaoProfile fetchKakaoProfile(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("id") == null) {
            throw new IllegalArgumentException("Could not verify Kakao user info.");
        }

        return KakaoProfile.from(body);
    }

    private Users updateSocialUser(Users user, KakaoProfile kakaoProfile) {
        user.setName(kakaoProfile.nickname());
        user.setEmail(kakaoProfile.email());
        user.setProvider(PROVIDER);
        ensureDefaultRole(user);
        return userRepository.save(user);
    }

    private Users updateExistingEmailUser(Users user, KakaoProfile kakaoProfile) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(kakaoProfile.nickname());
        }
        ensureDefaultRole(user);
        return userRepository.save(user);
    }

    private Users createSocialUser(String socialUserId, KakaoProfile kakaoProfile) {
        Users user = new Users();
        user.setUserId(socialUserId);
        user.setEmail(kakaoProfile.email());
        user.setName(kakaoProfile.nickname());
        user.setProvider(PROVIDER);
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode("SOCIAL_LOGIN_" + UUID.randomUUID()));
        return userRepository.save(user);
    }

    private void ensureDefaultRole(Users user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
    }

    public record LoginResult(Users user, boolean newUser) {
    }

    private record KakaoProfile(Long id, String nickname, String email) {
        @SuppressWarnings("unchecked")
        static KakaoProfile from(Map<String, Object> attributes) {
            Long id = ((Number) attributes.get("id")).longValue();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = kakaoAccount == null
                    ? null
                    : (Map<String, Object>) kakaoAccount.get("profile");

            String nickname = profile == null ? null : (String) profile.get("nickname");
            if (nickname == null || nickname.isBlank()) {
                nickname = "KakaoUser";
            }

            String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
            if (email == null || email.isBlank()) {
                email = "kakao_" + id + "@kakao.local";
            }

            return new KakaoProfile(id, nickname, email);
        }
    }
}
