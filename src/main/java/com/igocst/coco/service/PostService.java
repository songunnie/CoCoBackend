package com.igocst.coco.service;

import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.post.*;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import com.nhncorp.lucy.security.xss.XssPreventer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 생성
    @Transactional
    public ResponseEntity<PostSaveResponseDto> createPost(PostSaveRequestDto postSaveRequestDto, MemberDetails memberDetails) {
        // 1. 회원 id 가져온다
        // 2. 회원-게시글 연관관계 메소드를 사용해서 양쪽에 값을 넣어준다 (Member, Post)
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        // 게시글 길이 2000자 제한
        if (postSaveRequestDto.getContent().length() > 2000) {
            log.error("nickname={}, error={}", member.getNickname(), "게시글 길이 2000자 초과");
            return new ResponseEntity<>(
                    PostSaveResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }

        Post post = Post.builder()
                .title(XssPreventer.escape(postSaveRequestDto.getTitle()))
                .content(XssPreventer.escape(postSaveRequestDto.getContent()))
                .meetingType(postSaveRequestDto.getMeetingType())
                .contact(postSaveRequestDto.getContact())
                .period(postSaveRequestDto.getPeriod())
                .build();

        // 회원과 게시글 연관관계 메소드 사용해서 넣어줌
        member.addPost(post);   // 양방향 값 세팅
        postRepository.save(post);

        return new ResponseEntity<>(
                PostSaveResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 게시글 내용(상세) 조회
    @Transactional
    public ResponseEntity<PostReadResponseDto> readPost(Long postId, MemberDetails memberDetails, HttpServletRequest request, HttpServletResponse response) {
        // 로그인된 사용자의 정보가 필요 (로그인된 사용자가 아니면 접근 불가, JWT로 인증이 안되면 접근 불가)

        // 1. postId에 해당하는 게시글 조회
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            log.error("nickname={}, error={}", memberDetails.getNickname(), "해당 게시글 존재하지 않음");
            return new ResponseEntity<>(
                    PostReadResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Post findPost = postOptional.get();

        boolean enableUpdate = false;
        boolean enableDelete = false;

        // 2. 현재 로그인한 회원이 해당 게시글을 작성한 회원이 맞다면 수정, 삭제 가능
        if (memberDetails.getMember().getId() == findPost.getMember().getId()) {
            enableUpdate = true;
            enableDelete = true;
        }

        // 3. 현재 로그인한 회원이 관리자면, 모든 게시글 삭제 가능
        MemberRole memberRole = MemberRole.MEMBER;
        if (memberDetails.getMember().getRole().equals(MemberRole.ADMIN)) {
            memberRole = MemberRole.ADMIN;
            enableDelete = true;
        }

        // 조회 수 중복 방지
        updateHits(postId, request, response);

        return new ResponseEntity<>(
                PostReadResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .id(findPost.getId())
                        .title(findPost.getTitle())
                        .content(findPost.getContent())
                        .meetingType(findPost.getMeetingType())
                        .contact(findPost.getContact())
                        .period(findPost.getPeriod())
                        .recruitmentState(findPost.isRecruitmentState())
                        .hits(findPost.getHits())
                        .postDate(findPost.getCreateDate())
                        .writer(findPost.getMember().getNickname())
                        .githubUrl(findPost.getMember().getGithubUrl())
                        .portfolioUrl(findPost.getMember().getPortfolioUrl())
                        .introduction(findPost.getMember().getIntroduction())
                        .profileImageUrl(findPost.getMember().getProfileImageUrl())
                        .enableUpdate(enableUpdate)
                        .enableDelete(enableDelete)
                        .memberRole(memberRole)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 조회 수 중복 방지
    private void updateHits(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postView")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("["+ postId.toString() +"]")) {
                increaseHits(postId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + postId + "]");
                oldCookie.setPath("/");
                oldCookie.setDomain("cocoding.xyz");
                oldCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(oldCookie);
            }
        } else {
            increaseHits(postId);
            Cookie newCookie = new Cookie("postView", "[" + postId + "]");
            newCookie.setPath("/");
            newCookie.setDomain("cocoding.xyz");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }
    }

    // 조회수 증가 로직
    @Transactional
    public int increaseHits(Long id) {
        return postRepository.updateHits(id);
    }

    // 게시글 목록 전체 조회
    public ResponseEntity<List<PostReadResponseDto>> readPostList() {
        // 1. 게시글을 다 가져온다
        List<Post> posts = postRepository.findAllByOrderByCreateDateDesc();
        // 2. 가져온 게시글을 DTO 목록에 담아 반환한다.
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
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(postList, HttpStatus.valueOf(StatusCode.SUCCESS));

    }

    // 게시글 목록 조회 (모집 중인거만)
    public ResponseEntity<List<PostReadResponseDto>> readRecruitingPostList() {
        List<Post> recrutingPosts = postRepository.findAllByRecruitmentStateFalseOrderByCreateDateDesc();
        List<PostReadResponseDto> recrutingPostList = new ArrayList<>();

        for (Post post : recrutingPosts) {
            recrutingPostList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(recrutingPostList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 조회수순 게시글 목록 조회 (모집 중인거만)
    public ResponseEntity<List<PostReadResponseDto>> readRecruitingHitsPostList() {
        List<Post> recrutingHitsPosts = postRepository.findAllByRecruitmentStateFalseOrderByHitsDesc();
        List<PostReadResponseDto> recrutingHitsPostList = new ArrayList<>();
        for (Post post : recrutingHitsPosts) {
            recrutingHitsPostList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(recrutingHitsPostList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 조회수순 게시글 목록 조회 (모집 마감 포함)
    public ResponseEntity<List<PostReadResponseDto>> readHitsPostList() {
        List<Post> hitsPosts = postRepository.findAllByOrderByHitsDesc();
        List<PostReadResponseDto> hitsPostList = new ArrayList<>();
        for (Post post : hitsPosts) {
            hitsPostList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(hitsPostList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 댓글 많은 순으로 게시글 목록 조회 (모집 중인거만)
    public ResponseEntity<List<PostReadResponseDto>> readRecruitingCommentsPostList() {
        List<Post> recrutingPosts = postRepository.findAllByRecruitmentStateFalse();
        // 댓글 많은 순으로 정렬
        getCommentsPosts(recrutingPosts);

        List<PostReadResponseDto> recruitCommentsPostList = new ArrayList<>();
        for (Post post : recrutingPosts) {
            recruitCommentsPostList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(recruitCommentsPostList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 댓글 많은 순으로 게시글 목록 조회 (모집 마감 포함)
    public ResponseEntity<List<PostReadResponseDto>> readCommentsPostList() {
        // 1. 모든 게시글 가져온다
        // 2. 각 게시글의 달린 댓글을 검사
        // 3. 댓글이 많은 순으로 정렬?

        List<Post> posts = postRepository.findAll();
        // 댓글 많은 순으로 정렬
        getCommentsPosts(posts);

        List<PostReadResponseDto> commentsPostList = new ArrayList<>();
        for (Post post : posts) {
            commentsPostList.add(PostReadResponseDto.builder()
                    .status(StatusMessage.SUCCESS)
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .meetingType(post.getMeetingType())
                    .contact(post.getContact())
                    .period(post.getPeriod())
                    .recruitmentState(post.isRecruitmentState())
                    .hits(post.getHits())
                    .postDate(post.getCreateDate())
                    .writer(post.getMember().getNickname())
                    .build()
            );
        }
        return new ResponseEntity<>(commentsPostList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 댓글수 내림차순으로 게시글 정렬
    private List<Post> getCommentsPosts(List<Post> posts) {
        for (int i = 0; i< posts.size()-1; i++) {
            boolean swap = false;

            for (int j = 0; j< posts.size()-1-i; j++) {
                if(posts.get(j).getComments().size() < posts.get(j+1).getComments().size()) {
                    Collections.swap(posts, j, j+1);
                    swap = true;
                }
            }
            if (swap == false) {
                break;
            }
        }
        return posts;
    }

    //  게시글 수정
    @Transactional
    public ResponseEntity<PostUpdateResponseDto> updatePost(Long postId, PostUpdateRequestDto requestDto, MemberDetails memberDetails) {
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        // Member와 Post는 연관관계가 맺어져 있으므로, Member에 Post가 있다
        // 1. 로그인된 회원의 게시글 목록에서 수정할 postId의 게시글을 찾아온다.
        Optional<Post> postOptional = member.findPost(postId);

        if (postOptional.isEmpty()) {
            log.error("nickname={}, error={}", member.getNickname(), "수정할 게시글이 존재하지 않음");
            return new ResponseEntity<>(
                    PostUpdateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Post findPost = postOptional.get();

        if (requestDto.getContent().length() > 2000) {
            log.error("nickname={}, error={}", member.getNickname(), "게시글 길이 2000자 초과");
            return new ResponseEntity<>(
                    PostUpdateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        // 2. 가져온 Post를 requestDto로 값을 바꿔준다.
        findPost.updatePost(requestDto);

        return new ResponseEntity<>(
                PostUpdateResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    //     게시글 삭제
    @Transactional
    public ResponseEntity<PostDeleteResponseDto> deletePost(Long postId, MemberDetails memberDetails) {
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        // 연관관계 메소드
        // 1. 해당 게시글(postId)을 작성한, 로그인된 사용자(member)가 해당 게시글(postId)을 삭제한다. (postId, memberId)
        boolean isValid = member.deletePost(postId);

        if(!isValid) {
            log.error("nickname={}, error={}", member.getNickname(), "삭제할 게시글이 존재하지 않음");
            return new ResponseEntity<>(
                    PostDeleteResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        // 위의 예외처리를 통과하면 일치하는 사용자이므로, 해당 게시글을 삭제한다.
        postRepository.deleteById(postId);

        return new ResponseEntity<>(
                PostDeleteResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 관리자, 게시글 삭제 기능
    @Transactional
    public ResponseEntity<PostDeleteResponseDto> adminDeletePost(Long postId, MemberDetails memberDetails) {
        if (!memberDetails.getMember().getRole().equals(MemberRole.ADMIN)) {
            log.error("nickname={}, error={}", memberDetails.getNickname(), "관리자 권한이 없음");
            return new ResponseEntity<>(
                    PostDeleteResponseDto.builder().status(StatusMessage.FORBIDDEN_USER).build(),
                    HttpStatus.valueOf(StatusCode.FORBIDDEN_USER)
            );
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            log.error("nickname={}, error={}", memberDetails.getNickname(), "해당 게시글이 존재하지 않음");
            return new ResponseEntity<>(
                    PostDeleteResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Post findPost = postOptional.get();
        Member member = findPost.getMember();

        boolean isValid = member.deletePost(postId);
        if(!isValid) {
            log.error("nickname={}, error={}", memberDetails.getNickname(), "삭제할 게시글이 존재하지 않음");
            return new ResponseEntity<>(
                    PostDeleteResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        postRepository.deleteById(postId);

        return new ResponseEntity<>(
                PostDeleteResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 게시글 검색
    public ResponseEntity<List<PostReadResponseDto>> searchPost(String query) {
        // 검색값이 포함되어 있는 게시글을 가져옮
        List<Post> searchPosts = postRepository.findAllByTitleContainingOrderByCreateDateDesc(query);
        List<PostReadResponseDto> searchList = new ArrayList<>();
        for (Post post : searchPosts) {
            searchList.add(PostReadResponseDto.builder()
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
        return new ResponseEntity<>(searchList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }
}
