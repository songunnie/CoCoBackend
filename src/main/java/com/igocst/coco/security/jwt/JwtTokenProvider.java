package com.igocst.coco.security.jwt;

import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.MemberDetailsService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String SECRET_KEY = "COCO";
    private static final int EXPIRATION_MS = 3600000;
    private final MemberDetailsService memberDetailsService;


    // JWT 토큰 생성
    public static String generateToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // JWT 토큰에서 회원 정보 가져오기
    public static String getMemberEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(getMemberEmailFromToken(token));
        return new UsernamePasswordAuthenticationToken(memberDetails, "", null);  // 권한은 아직
    }

    // http 요청 헤더에서 JWT 토큰 값 가져오기
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
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
