package com.tjg_project.candy.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT AccessToken 전용 유틸리티
 */
@Component
public class JwtUtil {

    private final Key key;

    // ✅ AccessToken: 10분
    private final long accessTokenExpiration = 1000 * 60 * 10;
// ✅ AccessToken: 10분
//    private final long accessTokenExpiration = 1000 * 5;

    // ✅ secret이 주입된 뒤 Key 생성
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ✅ AccessToken 생성 (10분)
     */
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .claim("id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ 토큰에서 사용자 id 추출
     */
    public Long extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", Long.class);
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("❌ JWT expired: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("❌ JWT malformed: " + e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("❌ JWT signature invalid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ JWT invalid: " + e.getMessage());
        }
        return false;
    }
}
