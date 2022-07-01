package com.igocst.coco.controller;

import com.igocst.coco.dto.member.LoginRequestDto;
import com.igocst.coco.dto.member.LoginResponseDto;
import com.igocst.coco.dto.member.RegisterRequestDto;
import com.igocst.coco.dto.member.RegisterResponseDto;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import com.igocst.coco.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        return memberService.login(requestDto);
    }

    // 회원가입
    @PostMapping("/user")
    public RegisterResponseDto register(@RequestBody RegisterRequestDto requestDto) throws Exception {
        return memberService.register(requestDto);
    }

    // test, 시큐리티 필터를 통과, MemberDetails = 인증(로그인)된 사용자
    @GetMapping("/user/test")
    public String tokenTest(HttpServletRequest request,
                            @AuthenticationPrincipal MemberDetails memberDetails) {
        String token = request.getHeader("X-AUTH-TOKEN");
        System.out.println(memberDetails.getUsername());
        return JwtTokenProvider.getMemberEmailFromToken(token);
    }
}