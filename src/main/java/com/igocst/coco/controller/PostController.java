package com.igocst.coco.controller;


import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.post.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping("/post")
    public PostSaveResponseDto createPost(@RequestBody PostSaveRequestDto requestDto,
                                          @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.createPost(requestDto, memberDetails);
    }

    // 게시글 상세 조회
    @GetMapping("/post/{postId}")
    public PostReadResponseDto readPost(@PathVariable Long postId) {
        return postService.readPost(postId);
    }

    // 게시글 전체 목록 조회
    @GetMapping("/post/list")
    public List<PostReadResponseDto> readPostList() {
        return postService.readPostList();
    }

    // 게시글 수정
    @PutMapping("/post/{postId}")
    public PostUpdateResponseDto updatePost(@PathVariable Long postId,
                                            @RequestBody PostUpdateRequestDto requestDto,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.updatePost(postId, requestDto, memberDetails);
    }

    // 게시글 삭제
    @DeleteMapping("/post/{postId}")
    public PostDeleteResponseDto deletePost(@PathVariable Long postId,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.deletePost(postId, memberDetails);
    }

    // 모집 마감
    @PutMapping("/post/close/{postId}")
    public RecruitmentEndResponseDto recruitmentEnd(@PathVariable Long postId,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.recruitmentEnd(postId, memberDetails);
    }

    // 관리자, 게시글 삭제
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/post/{postId}")
    public PostDeleteResponseDto adminDeletePost(@PathVariable Long postId) {
        return postService.adminDeletePost(postId);
    }
}
