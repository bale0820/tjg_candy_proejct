package com.tjg_project.candy.domain.auth.dto;

import com.tjg_project.candy.domain.user.entity.Users;

public record SocialLoginResponse(
        String accessToken,
        String refreshToken,
        Long userPk,
        String userId,
        String name,
        String email,
        String provider,
        String role,
        boolean newUser
) {
    public static SocialLoginResponse of(
            String accessToken,
            String refreshToken,
            Users user,
            boolean newUser
    ) {
        return new SocialLoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getRole(),
                newUser
        );
    }
}
