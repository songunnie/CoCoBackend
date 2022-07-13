package com.igocst.coco.service;


import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.member.*;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
//@Slf4j
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String ADMIN_TOKEN = "sflIQ7FG381ei013i/SDGLYFuFua";   // 임시 관리자 토큰

    // 로그인
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto requestDto) {
        // requestDto에 담긴 로그인 정보들이 DB에 있는지 확인
        Member findMember = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("일치하는 사용자가 없습니다.")
//                () -> new ResponseEntity<>(StatusMessage.INVALID_PARAM, HttpStatus.valueOf(StatusCode.INVALID_PARAM))
        );

        // BCrypt으로 암호화된 비밀번호를 비교
        if (!passwordEncoder.matches(requestDto.getPassword(), findMember.getPassword())) {
//            throw new IllegalArgumentException("유효하지 않은 비밀번호");
            return new ResponseEntity<>(
                    LoginResponseDto.builder()
                            .status(StatusMessage.INVALID_PARAM)
                            .build(),
                    HttpStatus.valueOf(StatusCode.INVALID_PARAM)
            );
        }

        // JWT 토큰 만들어서 반환하기
        String token = JwtTokenProvider.generateToken(requestDto.getEmail());
//        log.info(token);
        return new ResponseEntity<>(
                LoginResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .token(token)
                    .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
//        return LoginResponseDto.builder()
//                .token(token)
//                .build();
    }


    // 회원가입
    public ResponseEntity<RegisterResponseDto> register(RegisterRequestDto requestDto) {
        // 회원 DB에 중복된 이메일이 있으면 에러
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            return new ResponseEntity<>(
                    RegisterResponseDto.builder()
                            .status(StatusMessage.DUPLICATED_USER)
                            .build(),
                    HttpStatus.valueOf(StatusCode.DUPLICATED_USER)
            );
//            IllegalArgumentException("중복된 이메일을 가진 회원이 존재합니다.");
        }

        // 회원 DB에 중복된 닉네임이 있으면 에러
        if (memberRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            return new ResponseEntity<>(
                    RegisterResponseDto.builder()
                            .status(StatusMessage.DUPLICATED_USER)
                            .build(),
                    HttpStatus.valueOf(StatusCode.DUPLICATED_USER)
            );
        }

        // 1. 권한 확인
        MemberRole role = MemberRole.MEMBER;
        if (requestDto.isAdmin()) {
            if (!requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
//                throw new IllegalArgumentException("관리자 토큰이 일치하지 않습니다.");
                return new ResponseEntity<>(
                        RegisterResponseDto.builder()
                                .status(StatusMessage.INVALID_PARAM)
                                .build(),
                        HttpStatus.valueOf(StatusCode.INVALID_PARAM)
                );
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
                .introduction((requestDto.getIntroduction()))
                .role(role) // 권한 추가 (ADMIN / MEMBER)
                .build();

        // 3. 생성한 member를 회원 DB에 저장
        memberRepository.save(member);

        return new ResponseEntity<>(
                RegisterResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    //회원 정보 획득
    public ResponseEntity<MemberReadResponseDto> readMember(MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        return new ResponseEntity<>(
                MemberReadResponseDto.builder()
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .githubUrl(member.getGithubUrl())
                    .portfolioUrl(member.getPortfolioUrl())
                    .introduction(member.getIntroduction())
                    .status(StatusMessage.SUCCESS)
                    .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 회원 정보 수정
    @Transactional
    public ResponseEntity<MemberUpdateResponseDto> updateMember(MemberUpdateRequestDto memberUpdateRequestDto,
                                                MemberDetails memberDetails) {
        //멤버를 찾고
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        if (member == null) {
//            throw new RuntimeException("프로필 수정 권한이 없습니다.");
            return new ResponseEntity<>(
                    MemberUpdateResponseDto.builder()
                            .status(StatusMessage.FORBIDDEN_USER)
                            .build(),
                    HttpStatus.valueOf(StatusCode.FORBIDDEN_USER)
            );
        }
        
        // TODO: password 수정하려면 기존 비번 확인하는거 필요, imageUrl도 추가해야함.
        //그 멤버의 정보를 바꾼다.
        member.updateNickname(memberUpdateRequestDto.getNickname());
        member.updateGithubUrl(memberUpdateRequestDto.getGithubUrl());
        member.updatePortfolioUrl(memberUpdateRequestDto.getPortfolioUrl());
        member.updateIntroduction(memberUpdateRequestDto.getIntroduction());

        return new ResponseEntity<>(
                MemberUpdateResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 회원 탈퇴
    @Transactional
    public ResponseEntity<MemberDeleteResponseDto> deleteMember(MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        if (member == null) {
//            throw new NullPointerException("회원이 아닙니다.");
            return new ResponseEntity<>(
                    MemberDeleteResponseDto.builder()
                            .status(StatusMessage.FORBIDDEN_USER)
                            .build(),
                    HttpStatus.valueOf(StatusCode.FORBIDDEN_USER)
            );
        }

//        if (member.getId() != userId) {  // 예외처리를 이정도로 해줘야 하는지, 안해도 되는건지 모르겠음.
//            throw new IllegalArgumentException("권한이 없습니다.");
//        }

        memberRepository.deleteById(member.getId());

        return new ResponseEntity<>(
                MemberDeleteResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }



    // 관리자, 회원 강제 탈퇴
    public ResponseEntity<MemberDeleteResponseDto> adminDeleteMember(Long userId) {
        memberRepository.deleteById(userId);

        return new ResponseEntity<>(
                MemberDeleteResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 이메일 중복 체크
    public ResponseEntity<CheckDupResponseDto> checkEmailDup(CheckEmailDupRequestDto checkEmailDupRequestDto) {
        // 이메일이 중복이면 true, 아니면 false
        String email = checkEmailDupRequestDto.getEmail();
        boolean isDup = memberRepository.findByEmail(email).isPresent();

        return new ResponseEntity<>(
                CheckDupResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .isDup(isDup)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 닉네임 중복 체크
    public ResponseEntity<CheckDupResponseDto> checkNicknameDup(CheckNicknameDupRequestDto checkNicknameDupRequestDto) {
        // 닉네임이 중복이면 true, 아니면 false
        String nickname = checkNicknameDupRequestDto.getNickname();
        boolean isDup = memberRepository.findByNickname(nickname).isPresent();

        return new ResponseEntity<>(
                CheckDupResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .isDup(isDup)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }
}
