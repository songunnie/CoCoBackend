package com.igocst.coco.controller;

import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.member.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import com.igocst.coco.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        // 로그인에 성공하면 클라이언트로, 생성한 JWT 토큰을 보낸다.
        return memberService.login(requestDto);
    }

    // 회원가입
    @PostMapping("/register")
    public RegisterResponseDto register(@RequestBody RegisterRequestDto requestDto) {
        return memberService.register(requestDto);
    }

    // test, 요청이 성공하면 시큐리티 필터를 통과했다는 의미, MemberDetails = 인증(로그인)된 사용자
    @GetMapping("/user/test")
    public String tokenTest(@AuthenticationPrincipal MemberDetails memberDetails) {
//        String token = request.getHeader("X-AUTH-TOKEN");
        String username = memberDetails.getUsername();
        System.out.println(username);
        return username;
    }

    //회원 정보 획득
    @GetMapping("/user")
    public MemberReadResponseDto readMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.readMember(memberDetails);
    }
    //회원 정보 수정
    @PutMapping("/user")
    public MemberUpdateResponseDto updateMember(@RequestBody MemberUpdateRequestDto memberUpdateRequestDto,
                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.updateMember(memberUpdateRequestDto, memberDetails);
    }

    // 회원 탈퇴
    @DeleteMapping("/user")
    public MemberDeleteResponseDto deleteMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.deleteMember(memberDetails);
    }


    // test, 관리자만 사용할 수 있는 URL
    @Secured(MemberRole.Authority.ADMIN)
    @GetMapping("/user/test2")
    public String adminTest(@AuthenticationPrincipal MemberDetails memberDetails) {
        String nickname = memberDetails.getNickname();
        System.out.println(nickname);
        return nickname;
    }

    // 관리자, 회원 강제 탈퇴
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/user/{userId}")
    public MemberDeleteResponseDto adminDeleteUser(@PathVariable Long userId) {
        return memberService.adminDeleteMember(userId);
    }

    // 이메일 중복 체크
    @PostMapping("/register/check-email")
    public CheckDupResponseDto checkEmailDup(@RequestBody CheckEmailDupRequestDto checkEmailDupRequestDto) {
        return memberService.checkEmailDup(checkEmailDupRequestDto);
    }

    // 닉네임 중복 체크
    @PostMapping("/register/check-nickname")
    public CheckDupResponseDto checkNicknameDup(@RequestBody CheckNicknameDupRequestDto checkNicknameDupRequestDto) {
        return memberService.checkNicknameDup(checkNicknameDupRequestDto);
    }
}