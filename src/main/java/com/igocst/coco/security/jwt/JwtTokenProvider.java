package com.igocst.coco.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;

import java.util.Date;

public class JwtTokenProvider {

    private static final String SECRET_KEY = "COCO";
    private static final int EXPIRATION_MS = 3600000;

    // JWT 토큰 생성
    public static String generateToken(Authentication authentication) {
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // JWT 토큰 추출
    public static String getMemberEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // JWT 토큰 예외 검사
    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e){
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 잘못되었습니다.");
        } catch (Exception e) {
            System.out.println("JWT 예외 발생");
        }
        return false;
    }
}
