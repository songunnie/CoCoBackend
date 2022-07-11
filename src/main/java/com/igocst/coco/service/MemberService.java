package com.igocst.coco.service;


import com.igocst.coco.domain.Comment;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.comment.CommentReadResponseDto;
import com.igocst.coco.dto.comment.CommentUpdateRequestDto;
import com.igocst.coco.dto.comment.CommentUpdateResponseDto;
import com.igocst.coco.dto.member.*;
import com.igocst.coco.dto.message.MessageDeleteResponseDto;
import com.igocst.coco.dto.message.MessageReadResponseDto;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String ADMIN_TOKEN = "sflIQ7FG381ei013i/SDGLYFuFua";   // 임시 관리자 토큰

    // 로그인
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // requestDto에 담긴 로그인 정보들이 DB에 있는지 확인
        Member findMember = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("일치하는 사용자가 없습니다.")
        );

        // BCrypt으로 암호화된 비밀번호를 비교
        if (!passwordEncoder.matches(requestDto.getPassword(), findMember.getPassword())) {
            throw new IllegalArgumentException("유효하지 않은 비밀번호");
        }

        // JWT 토큰 만들어서 반환하기
//        Authentication authentication = new UsernamePasswordAuthenticationToken(requestDto.getEmail(), null, null);
//        String token = JwtTokenProvider.generateToken(authentication);
        String token = JwtTokenProvider.generateToken(requestDto.getEmail());

        return LoginResponseDto.builder()
                .status("200")
                .token(token)
                .build();
    }


    // 회원가입
    public RegisterResponseDto register(RegisterRequestDto requestDto) {
        // 회원 DB에 중복된 이메일이 있으면 에러
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("중복된 이메일을 가진 회원이 존재합니다.");
        }

        // 회원 DB에 중복된 닉네임이 있으면 에러
        if (memberRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임을 가진 회원이 존재합니다.");
        }

        // 1. 권한 확인
        MemberRole role = MemberRole.MEMBER;
        if (requestDto.isAdmin()) {
            if (!requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new IllegalArgumentException("관리자 토큰이 일치하지 않습니다.");
            }
            role = MemberRole.ADMIN;
        }

        // 2. requestDto에 담긴 회원 가입 정보들, 확인한 권한정보를 바탕으로 Member를 만든다
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .githubUrl(requestDto.getGithubUrl())
                .portfolioUrl(requestDto.getPortfolioUrl())
                .role(role) // 권한 추가 (ADMIN / MEMBER)
                .build();

        // 3. 생성한 member를 회원 DB에 저장
        memberRepository.save(member);

        return RegisterResponseDto.builder()
                .status("200")
                .build();
    }

    //회원 정보 획득
    public MemberReadResponseDto readMember(MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));
        
        return MemberReadResponseDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .githubUrl(member.getGithubUrl())
                .portfolioUrl(member.getPortfolioUrl())
                .introduction(member.getIntroduction())
                .status("회원 정보 불러오기 완료")
                .build();
    }

    // 회원 정보 수정
    @Transactional
    public MemberUpdateResponseDto updateMember(MemberUpdateRequestDto memberUpdateRequestDto,
                                                MemberDetails memberDetails) {
        //멤버를 찾고
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        if (member == null) {
            throw new RuntimeException("프로필 수정 권한이 없습니다.");
        }
        
        // TODO: password 수정하려면 기존 비번 확인하는거 필요, imageUrl도 추가해야함.
        //그 멤버의 정보를 바꾼다.
        member.updateNickname(memberUpdateRequestDto.getNickname());
        member.updateGithubUrl(memberUpdateRequestDto.getGithubUrl());
        member.updatePortfolioUrl(memberUpdateRequestDto.getPortfolioUrl());
        member.updateIntroduction(memberUpdateRequestDto.getIntroduction());

        return MemberUpdateResponseDto.builder()
                .status("회원 정보가 수정되었습니다")
                .build();
    }

    // 회원 탈퇴
    @Transactional
    public MemberDeleteResponseDto deleteMember(MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        if (member == null) {
            throw new NullPointerException("회원이 아닙니다.");
        }

//        if (member.getId() != userId) {  // 예외처리를 이정도로 해줘야 하는지, 안해도 되는건지 모르겠음.
//            throw new IllegalArgumentException("권한이 없습니다.");
//        }

        memberRepository.deleteById(member.getId());

        return MemberDeleteResponseDto.builder()
                .userId(member.getId())
                .status("회원 탈퇴했습니다.")
                .build();
    }



    // 관리자, 회원 강제 탈퇴
    public MemberDeleteResponseDto adminDeleteMember(Long userId) {
        memberRepository.deleteById(userId);

        return MemberDeleteResponseDto.builder()
                .status("200")
                .build();
    }

    // 이메일 중복 체크
    public CheckDupResponseDto checkEmailDup(CheckEmailDupRequestDto checkEmailDupRequestDto) {
        // 이메일이 중복이면 true, 아니면 false
        String email = checkEmailDupRequestDto.getEmail();
        boolean isDup = memberRepository.findByEmail(email).isPresent();

        return CheckDupResponseDto.builder()
                .isDup(isDup)
                .build();
    }

    // 닉네임 중복 체크
    public CheckDupResponseDto checkNicknameDup(CheckNicknameDupRequestDto checkNicknameDupRequestDto) {
        // 닉네임이 중복이면 true, 아니면 false
        String nickname = checkNicknameDupRequestDto.getNickname();
        boolean isDup = memberRepository.findByNickname(nickname).isPresent();

        return CheckDupResponseDto.builder()
                .isDup(isDup)
                .build();
    }
}
