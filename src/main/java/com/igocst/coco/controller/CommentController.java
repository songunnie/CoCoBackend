package com.igocst.coco.controller;

import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.comment.*;
import com.igocst.coco.dto.post.PostReadResponseDto;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentCreateResponseDto> createComment(@RequestBody CommentCreateRequestDto commentCreateRequestDto,
                                                                 @PathVariable Long postId,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.createComment(commentCreateRequestDto, postId, memberDetails);
    }

    // 댓글 조회
    @GetMapping("/{postId}/comment/list")
    public ResponseEntity<List<CommentReadResponseDto>> readComment(@PathVariable Long postId,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.readCommentList(postId, memberDetails);
    }

    // 댓글 수정
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<CommentUpdateResponseDto> updateComment(@RequestBody CommentUpdateRequestDto commentUpdateRequestDto,
                                                  @PathVariable Long commentId,
                                                  @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.updateComment(commentUpdateRequestDto, commentId, memberDetails);
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<CommentDeleteResponseDto> deleteComment(@PathVariable Long commentId,
                                                  @AuthenticationPrincipal MemberDetails memberDetails){
        return commentService.deleteComment(commentId, memberDetails); //.removeCommentById? vs removeComment
    }

    // 관리자, 댓글 삭제
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/comment/{commentId}")
    public ResponseEntity<CommentDeleteResponseDto> adminDeletePost(@PathVariable Long commentId,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.adminDeleteComment(commentId, memberDetails);
    }
}
