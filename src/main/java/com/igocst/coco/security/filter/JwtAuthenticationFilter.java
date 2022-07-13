package com.igocst.coco.security.filter;

import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// JWT 사용을 위한 필터
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        try {
            // JWT 토큰을 얻어온다
            String token = jwtTokenProvider.resolveToken(request);

            // JWT 토큰 검사
            if (token != null && JwtTokenProvider.validateToken(token)) {
//                String email = JwtTokenProvider.getMemberEmailFromToken(token);

                // 시큐리티 인증
                // 아직 권한 설정하지 않음 (관리자, 일반 사용자)
                // UsernamePasswordAuthenticationToken 지양, 딴 거 쓰기
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, null);
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 완료
            }
//            } else {
//                request.setAttribute("unAuthorization", "인증실패");
//            }

//        } catch (Exception e) {
//            System.out.println("인증 예외 발생");
//        }

        filterChain.doFilter(request, response);
    }

    // http 요청에서 쿠키를 받아온다 (JWT 토큰이 포함되어 있음)
    private String getTokenFromRequest(HttpServletRequest request) {
        Cookie token = WebUtils.getCookie(request, "token");
        return token.getValue();
    }

}
