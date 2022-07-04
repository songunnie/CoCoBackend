package com.igocst.coco.controller;


import com.igocst.coco.dto.post.*;
import com.igocst.coco.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 로그인된 회원의 정보를 받아야 한다. (아직 완성 X)
     */

    // 게시글 생성
    @PostMapping
    public PostSaveResponseDto createPost(@RequestBody PostSaveRequestDto requestDto) {
        return postService.createPost(requestDto);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public PostReadResponseDto readPost(@PathVariable Long postId) {
        return postService.readPost(postId);
    }

    // 게시글 전체 목록 조회
    @GetMapping("/list")
    public List<PostReadResponseDto> readPostList() {
        return postService.readPostList();
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public PostUpdateResponseDto updatePost(@PathVariable Long postId, @RequestBody PostUpdateRequestDto requestDto) throws Exception {
        return postService.updatePost(postId, requestDto);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public PostDeleteResponseDto deletePost(@PathVariable Long postId) throws Exception {
        return postService.deletePost(postId);
    }

    // 모집 마감
    @PutMapping("/close/{postId}")
    public RecruitmentEndResponseDto recruitmentEnd(@PathVariable Long postId) {
        return postService.recruitmentEnd(postId);
    }
}
