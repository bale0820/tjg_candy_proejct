package com.tjg_project.candy.domain.auth.controller;

import com.tjg_project.candy.domain.auth.dto.KakaoAppLoginRequest;
import com.tjg_project.candy.domain.auth.dto.SocialLoginResponse;
import com.tjg_project.candy.domain.auth.entity.RefreshToken;
import com.tjg_project.candy.domain.auth.service.AuthService;
import com.tjg_project.candy.domain.auth.service.KakaoAppLoginService;
import com.tjg_project.candy.domain.user.entity.Users;
import com.tjg_project.candy.global.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/social")
public class SocialAuthController {
    private final KakaoAppLoginService kakaoAppLoginService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public SocialAuthController(
            KakaoAppLoginService kakaoAppLoginService,
            JwtUtil jwtUtil,
            AuthService authService
    ) {
        this.kakaoAppLoginService = kakaoAppLoginService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/kakao")
    public ResponseEntity<SocialLoginResponse> kakaoLogin(@RequestBody KakaoAppLoginRequest request) {
        KakaoAppLoginService.LoginResult result = kakaoAppLoginService.login(request.getAccessToken());
        Users user = result.user();
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        RefreshToken refresh = authService.createRefreshToken(user.getId());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", UUID.randomUUID().toString())
                .httpOnly(false)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        ResponseCookie roleCookie = ResponseCookie.from("role", user.getRole())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
                .body(SocialLoginResponse.of(accessToken, refresh.getToken(), user, result.newUser()));
    }

    @ExceptionHandler({IllegalArgumentException.class, RestClientException.class})
    public ResponseEntity<Map<String, String>> handleKakaoLoginException(Exception e) {
        return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
}
