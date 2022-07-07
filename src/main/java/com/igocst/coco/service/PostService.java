package com.igocst.coco.service;

import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.post.*;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 생성
    @Transactional
    public PostSaveResponseDto createPost(PostSaveRequestDto postSaveRequestDto, MemberDetails memberDetails) {
        // 어떤 회원이 게시글을 작성할건지 (로그인 되어있는 회원)

        // 1. 회원 id 가져온다
        // 2. 회원-게시글 연관관계 메소드를 사용해서 양쪽에 값을 넣어준다 (Member, Post)
        Member member = memberRepository.findById(memberDetails.getMember().getId()).orElseThrow(
                () -> new IllegalArgumentException("해당하는 회원 ID가 업습니다.")
        );

        Post post = Post.builder()
                .title(postSaveRequestDto.getTitle())
                .content(postSaveRequestDto.getContent())
                .meetingType(postSaveRequestDto.getMeetingType())
                .contact(postSaveRequestDto.getContact())
                .period(postSaveRequestDto.getPeriod())
                .build();

        // 회원과 게시글 연관관계 메소드 사용해서 넣어줌
        member.addPost(post);   // 양방향 값 세팅
//        post.addMember(member);  // 양방향 값 세팅
        postRepository.save(post);

        return PostSaveResponseDto.builder()
                .status("게시글 저장에 성공했습니다.")
                .build();
    }

    // 게시글 내용(상세) 조회
    public PostReadResponseDto readPost(Long postId) {
        // 로그인된 사용자의 정보가 필요 (로그인된 사용자가 아니면 접근 불가, JWT로 인증이 안되면 접근 불가)

        // 1. postId에 해당하는 게시글 조회
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        return PostReadResponseDto.builder()
                .status("200")
                .id(findPost.getId())
                .title(findPost.getTitle())
                .content(findPost.getContent())
                .meetingType(findPost.getMeetingType())
                .contact(findPost.getContact())
                .period(findPost.getPeriod())
                .state(findPost.isRecruitmentState())
                .hits(findPost.getHits())
                .postDate(findPost.getLastModifiedDate())
                .writer(findPost.getMember().getNickname())
                .build();
    }

    // 게시글 목록 전체 조회
    public List<PostReadResponseDto> readPostList() {
        // 1. 게시글을 다 가져온다
        List<Post> posts = postRepository.findAll();

        // 2. 가져온 게시글을 DTO 목록에 담아 반환한다.
        List<PostReadResponseDto> postList = new ArrayList<>();
        for (Post post : posts) {
            postList.add(PostReadResponseDto.builder()
                    .status("200")
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .state(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getLastModifiedDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return postList;
    }

    // 게시글 목록 조회 (모집 중인거만)
    public List<PostReadResponseDto> readRecruitingPostList() {
        List<Post> recrutingPosts = postRepository.findAllByRecruitmentStateTrue();

        List<PostReadResponseDto> recrutingPostList = new ArrayList<>();
        for (Post post : recrutingPosts) {
            recrutingPostList.add(PostReadResponseDto.builder()
                    .status("200")
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .state(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getLastModifiedDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return recrutingPostList;
    }

//  게시글 수정
    @Transactional
    public PostUpdateResponseDto updatePost(Long postId, PostUpdateRequestDto requestDto, MemberDetails memberDetails) {
        // 로그인된 사용자 정보가 필요
//        Member member = memberDetails.getMember();  // 영속성이 없는 상태

        // 영속성이 있는 상태
        Member member = memberRepository.findById(memberDetails.getMember().getId()).orElseThrow(
                () -> new IllegalArgumentException("유효한 회원이 아닙니다.")
        );

        // Member와 Post는 연관관계가 맺어져 있으므로, Member에 Post가 있다
        // 1. 로그인된 회원의 게시글 목록에서 수정할 postId의 게시글을 찾아온다.
        Post findPost = member.findPost(postId);
        if (findPost == null) {
            throw new RuntimeException("회원님이 작성한 게시글을 찾을 수 없습니다.");  // 어떤 예외처리를 해야할 지 잘 모르겠어서 일단 Exception
        }

        // 2. 가져온 Post를 requestDto로 값을 바꿔준다.
        findPost.updatePost(requestDto);

        return PostUpdateResponseDto.builder()
                .status("200")
                .build();
    }

//     게시글 삭제
    @Transactional
    public PostDeleteResponseDto deletePost(Long postId, MemberDetails memberDetails) {
        // 로그인된 사용자 정보가 필요
        Member member = memberRepository.findById(memberDetails.getMember().getId()).orElseThrow(
                () -> new IllegalArgumentException("유효한 회원이 아닙니다.")
        );

        // 연관관계 메소드
        // 1. 해당 게시글(postId)을 작성한, 로그인된 사용자(member)가 해당 게시글(postId)을 삭제한다. (postId, memberId)
        boolean isValid = member.deletePost(postId);

        if(!isValid) {
            throw new RuntimeException("삭제할 수 없습니다.");
        }

        // 위의 예외처리를 통과하면 일치하는 사용자이므로, 해당 게시글을 삭제한다.
        postRepository.deleteById(postId);

        return PostDeleteResponseDto.builder()
                .status("200")
                .build();
    }

    // 관리자, 게시글 삭제 기능
    @Transactional
    public PostDeleteResponseDto adminDeletePost(Long postId) {

        /**
         * 관리자가 임의로 게시글을 삭제한다.
         * 삭제할 게시글 id를 이용해 게시글을 가져오고, 그 게시글을 작성한 회원과 연관관계를 끊고 삭제시킨다.
        */
        Post findPost = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
//
        Member member = findPost.getMember();
        boolean isValid = member.deletePost(postId);

        if(!isValid) {
            throw new RuntimeException("삭제할 수 없습니다.");
        }

        postRepository.deleteById(postId);

        return PostDeleteResponseDto.builder()
                .status("200")
                .build();
    }
}
