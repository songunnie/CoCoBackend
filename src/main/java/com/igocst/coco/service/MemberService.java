package com.igocst.coco.service;


import com.igocst.coco.domain.Member;
import com.igocst.coco.dto.member.LoginRequestDto;
import com.igocst.coco.dto.member.LoginResponseDto;
import com.igocst.coco.dto.member.RegisterRequestDto;
import com.igocst.coco.dto.member.RegisterResponseDto;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // requestDto에 담긴 로그인 정보들이 DB에 있는지 확인
        Member findMember = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("일치하는 사용자가 없습니다.")
        );

        // BCrypt으로 암호화된 비밀번호를 비교
       if (passwordEncoder.matches(findMember.getPassword(), requestDto.getPassword())) {
            throw new IllegalArgumentException("유효하지 않은 비밀번호");
        }

        // JWT 토큰 만들어서 반환하기
        // ...
        Authentication authentication = new UsernamePasswordAuthenticationToken(requestDto.getEmail(), null, null);
        String token = JwtTokenProvider.generateToken(authentication);

        return LoginResponseDto.builder()
                .status("200")
                .token(token)
                .build();
    }


    // 회원가입
    public RegisterResponseDto register(RegisterRequestDto requestDto) throws Exception {
        // 1. requestDto에 담긴 회원 가입 정보들을 바탕으로 Member를 만든다
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .githubUrl(requestDto.getGithubUrl())
                .portfolioUrl(requestDto.getPortfolioUrl())
                .build();

        // 1-1. 회원 DB에 중복된 이메일이 있으면 에러
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new Exception("중복된 이메일이 존재합니다.");
        }

        // 2. 생성한 member를 회원 DB에 저장
        memberRepository.save(member);

        return RegisterResponseDto.builder()
                .status("200")
                .build();
    }

    // 회원 정보 수정
}
