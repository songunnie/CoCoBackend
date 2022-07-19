package com.igocst.coco.service;

import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.Comment;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.comment.CommentReadResponseDto;
import com.igocst.coco.dto.member.*;
import com.igocst.coco.dto.post.PostReadResponseDto;
import com.igocst.coco.repository.CommentRepository;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.s3.S3Service;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//@Slf4j
public class MemberService {

    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${secret.admin.token}")
    private String ADMIN_TOKEN;   // 임시 관리자 토큰

    // 로그인
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto requestDto) {
        // requestDto에 담긴 로그인 정보들이 DB에 있는지 확인
        Optional<Member> memberOptional = memberRepository.findByEmail(requestDto.getEmail());
        if (memberOptional.isEmpty()) {
            return new ResponseEntity<>(
                    LoginResponseDto.builder()
                            .status(StatusMessage.BAD_REQUEST)
                            .build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Member findMember = memberOptional.get();

        // BCrypt으로 암호화된 비밀번호를 비교
        if (!passwordEncoder.matches(requestDto.getPassword(), findMember.getPassword())) {
            return new ResponseEntity<>(
                    LoginResponseDto.builder()
                            .status(StatusMessage.INVALID_PARAM)
                            .build(),
                    HttpStatus.valueOf(StatusCode.INVALID_PARAM)
            );
        }

        // JWT 토큰 만들어서 반환하기
        String token = jwtTokenProvider.generateToken(requestDto.getEmail());

        return new ResponseEntity<>(
                LoginResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .token(token)
                    .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
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
                .profileImageUrl(requestDto.getProfileImageUrl())
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
        Member member = memberDetails.getMember();

        return new ResponseEntity<>(
                MemberReadResponseDto.builder()
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .githubUrl(member.getGithubUrl())
                    .portfolioUrl(member.getPortfolioUrl())
                    .introduction(member.getIntroduction())
                    .profileImageUrl(member.getProfileImageUrl())
                    .status(StatusMessage.SUCCESS)
                    .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 회원 정보 수정
    @Transactional
    public ResponseEntity<MemberUpdateResponseDto> updateMember(MemberUpdateRequestDto memberUpdateRequestDto,
                                                MemberDetails memberDetails) throws IOException {

        // acid
        //멤버를 찾고
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        //파일을 getfile로 해서 받음
        MultipartFile file = memberUpdateRequestDto.getFile();
        // 분기 처리
        if (file != null) {
            String fileUrl = s3Service.upload(file, "profileImage", memberDetails);
            member.updateProfileImage(fileUrl);
        }

        // TODO: Step 1. 똑같은 정보를 준건지, 하나라도 수정이 된건지 체크! -> 조건문으로 분기처리를해서 돌아가는지 테스트해봐야할듯.(DB에 최소한으로 다녀오기!)
        // TODO: Step 2. S3에 저장하기(S3에 저장해야 해당 파일에 대한 url에 반환되기 때문에!)
        // TODO: Step 3. DB에 저장하기(반환된 S3에 저장되어있는 url을 DB에 저장)
        // TODO: Step 4. 왜 IOException 같은 예외처리를 해줘야했는지 설명할 수 있을 정도로 파악해보기.

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
        Member member = memberDetails.getMember();
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

    // 프로필 모달에서 닉네임 중복체크
    public ResponseEntity<CheckDupResponseDto>  checkNicknameDupProfile(CheckNicknameDupRequestDto checkNicknameDupRequestDto,
                                                                        MemberDetails memberDetails) {

        // 찾은 멤버의 아이디로 멤버를 찾았으면, 멤버의 기존 닉네임을 얻어올 수 있다.
        Member member = memberDetails.getMember();

        //닉네임이 기존 닉네임과 같지 않을 때 동작
        String nickname = checkNicknameDupRequestDto.getNickname();
        //중복이면 true, 아니면 false
        boolean isDup = false;

        //받은 닉네임과 기존 닉네임이 다르면 = 닉네임을 바꾼것.
        //닉네임을 안바꾸면 if문을 안돌게 된다.
        //.equals는 주소값이 아니라 그 안에 들은 값을 직접 비교한다.

        if (memberRepository.findByNickname(nickname).isPresent()) {
            isDup = true;
        }

        if (nickname.equals(member.getNickname())) {
            isDup = false;
        }

        return new ResponseEntity<>(
                CheckDupResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .isDup(isDup)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 자신이 작성한 게시글 프로필에서 보여주기
    @Transactional
    public ResponseEntity<List<PostReadResponseDto>> readMyPosts(MemberDetails memberDetails) {

        // 1. 로그인한 사용자의 게시글 전부 가져와서 반환
        List<Post> posts = postRepository.findAllByMember_Id(memberDetails.getMember().getId());
        List<PostReadResponseDto> postList = new ArrayList<>();
        for (Post post : posts) {
            postList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getLastModifiedDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(postList, HttpStatus.valueOf(StatusCode.SUCCESS));

    }

    public ResponseEntity<List<CommentReadResponseDto>> readMyComments(MemberDetails memberDetails) {
        // 1. 로그인한 사용자의 모든 댓글을 가져와서 반환
        List<Comment> comments = commentRepository.findAllByMember_Id(memberDetails.getMember().getId());
        List<CommentReadResponseDto> commentList = new ArrayList<>();
        for (Comment comment : comments) {
            commentList.add(CommentReadResponseDto.builder()
                    .id(comment.getId())
                    .postId(comment.getPost().getId())
                    .comments(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .createDate(comment.getCreateDate())
                    .status(StatusMessage.SUCCESS)
                    .build());
        }
        return new ResponseEntity<>(commentList, HttpStatus.valueOf(StatusCode.SUCCESS));

    }
}
