package com.igocst.coco.security.jwt;

import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.MemberDetailsService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${secret.jwt.key}")
    private String secretKey;

    @Value("${secret.jwt.expiration}")
    private Long expiration;
    private final MemberDetailsService memberDetailsService;

    // JWT 토큰 생성
    public String generateToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + expiration);
        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // JWT 토큰에서 회원 정보 가져오기
    public String getMemberEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        // UserDetailsService에서 DB에 JWT 토큰으로 넣어둔 email과 일치하는 데이터가 있으면 가져온다. ( MemberDetails )
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(getMemberEmailFromToken(token));
        if (memberDetails.getMember() == null) {
            return null;
        }
        return new UsernamePasswordAuthenticationToken(memberDetails, "", memberDetails.getAuthorities());
    }

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

//     JWT 토큰이 Authorization Bearer 헤더에 있다
    public String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_AUTHORIZATION);
        if (headerValue == null) {
            return null;
        }
        if (!headerValue.startsWith(TOKEN_PREFIX)) {
            throw new IllegalArgumentException("잘못된 토큰 정보입니다.");
        }
        // 'Authorization Bearer '에 담겨있는 토큰을 가져온다
        return headerValue.substring(TOKEN_PREFIX.length());
    }

    // JWT 토큰 예외 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e){
            // 로그 찍어주기, 로그를 콘솔로? 파일로? 찍어줄 수 있다.
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        } catch (Exception e) {
            log.error("JWT 예외 발생");
        }
        return false;
    }
}