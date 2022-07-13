package com.igocst.coco.controller;

import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.member.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        // 로그인에 성공하면 클라이언트로, 생성한 JWT 토큰을 보낸다.
        return memberService.login(requestDto);
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto requestDto) {
        return memberService.register(requestDto);
//        return new ResponseEntity<>(StatusMessage.SUCCESS, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    //회원 정보 획득
    @GetMapping("/user")
    public ResponseEntity<MemberReadResponseDto> readMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.readMember(memberDetails);
    }
    //회원 정보 수정
    @PutMapping("/user")
    public ResponseEntity<MemberUpdateResponseDto> updateMember(@RequestBody MemberUpdateRequestDto memberUpdateRequestDto,
                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.updateMember(memberUpdateRequestDto, memberDetails);
    }

    // 회원 탈퇴
    @DeleteMapping("/user")
    public ResponseEntity<MemberDeleteResponseDto> deleteMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.deleteMember(memberDetails);
    }

    // 관리자, 회원 강제 탈퇴
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<MemberDeleteResponseDto> adminDeleteUser(@PathVariable Long userId) {
        return memberService.adminDeleteMember(userId);
    }

    // 이메일 중복 체크
    @PostMapping("/register/check-email")
    public ResponseEntity<CheckDupResponseDto> checkEmailDup(@RequestBody CheckEmailDupRequestDto checkEmailDupRequestDto) {
        return memberService.checkEmailDup(checkEmailDupRequestDto);
    }

    // 닉네임 중복 체크
    @PostMapping("/register/check-nickname")
    public ResponseEntity<CheckDupResponseDto> checkNicknameDup(@RequestBody CheckNicknameDupRequestDto checkNicknameDupRequestDto) {
        return memberService.checkNicknameDup(checkNicknameDupRequestDto);
    }
}