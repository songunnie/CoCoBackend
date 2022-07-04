package com.igocst.coco.service;


import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.member.LoginRequestDto;
import com.igocst.coco.dto.member.LoginResponseDto;
import com.igocst.coco.dto.member.RegisterRequestDto;
import com.igocst.coco.dto.member.RegisterResponseDto;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (passwordEncoder.matches(findMember.getPassword(), requestDto.getPassword())) {
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
    public RegisterResponseDto register(RegisterRequestDto requestDto) throws Exception {
        // 회원 DB에 중복된 이메일이 있으면 에러
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("중복된 회원이 존재합니다.");
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

    // 회원 정보 수정
}
