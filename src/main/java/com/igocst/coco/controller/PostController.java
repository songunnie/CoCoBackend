package com.igocst.coco.controller;

import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.post.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.PostService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping("/post")
    public ResponseEntity<PostSaveResponseDto> createPost(@RequestBody PostSaveRequestDto requestDto,
                                                         @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.createPost(requestDto, memberDetails);
    }

    // 게시글 상세 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<PostReadResponseDto> readPost(@PathVariable Long postId,
                                                        @AuthenticationPrincipal MemberDetails memberDetails,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        return postService.readPost(postId, memberDetails, request, response);
    }

    // 게시글 전체 목록 조회
    @GetMapping("/post/list")
    public ResponseEntity<List<PostReadResponseDto>> readPostList() {
        return postService.readPostList();
    }

    // 모집 중인 게시글 목록 조회
    @GetMapping("/post/recruit/list")
    public ResponseEntity<List<PostReadResponseDto>> readRecruitingPostList() {
        return postService.readRecruitingPostList();
    }

    // 게시글 수정
    @PutMapping("/post/{postId}")
    public ResponseEntity<PostUpdateResponseDto> updatePost(@PathVariable Long postId,
                                            @RequestBody PostUpdateRequestDto requestDto,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.updatePost(postId, requestDto, memberDetails);
    }

    // 게시글 삭제
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<PostDeleteResponseDto> deletePost(@PathVariable Long postId,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.deletePost(postId, memberDetails);
    }

    // 관리자, 게시글 삭제
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/post/{postId}")
    public ResponseEntity<PostDeleteResponseDto> adminDeletePost(@PathVariable Long postId,
                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        return postService.adminDeletePost(postId, memberDetails);
    }

    // 게시글 검색
    @GetMapping("/post/search")
    public ResponseEntity<List<PostReadResponseDto>> searchPost(@RequestParam String query) {
        return postService.searchPost(query);
    }
}
