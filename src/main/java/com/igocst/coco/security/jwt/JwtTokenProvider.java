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

    private static final String SECRET_KEY = "COCOPROJECTLETSGO";
    // 만료 시간 어떻게?, 1시간? 6시간? 하루?
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
        // UserDetailsService에서 DB에 JWT 토큰으로 넣어둔 email과 일치하는 데이터가 있으면 가져온다. ( MemberDetails )
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(getMemberEmailFromToken(token));
        return new UsernamePasswordAuthenticationToken(memberDetails, "", memberDetails.getAuthorities());
    }

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    // http 요청 헤더에서 JWT 토큰 값 가져오기
//    public String resolveToken(HttpServletRequest request) {
//        return request.getHeader("X-AUTH-TOKEN");
//    }

    public String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_AUTHORIZATION);
        if (headerValue == null) {
            throw new RuntimeException("토큰 정보가 없습니다.");
        }
//
//        return headerValue;

        if (!headerValue.startsWith(TOKEN_PREFIX)) {
            throw new IllegalArgumentException("잘못된 토큰 정보입니다.");
        }

        // 'Authorization Bearer '에 담겨있는 토큰을 가져온다
        return headerValue.substring(TOKEN_PREFIX.length());
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
