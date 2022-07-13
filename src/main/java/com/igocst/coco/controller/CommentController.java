package com.igocst.coco.controller;

import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.dto.comment.*;
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

    @PostMapping("/comment/{post_id}")
    public ResponseEntity<CommentCreateResponseDto> createComment(@RequestBody CommentCreateRequestDto commentCreateRequestDto,
                                                                 @PathVariable Long post_id,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.join(commentCreateRequestDto, post_id, memberDetails);
    }

    @GetMapping("/comment/list/{post_id}")
    public ResponseEntity<List<CommentReadResponseDto>> readComment(@PathVariable Long post_id,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.readCommentList(post_id, memberDetails);
    }

    @PutMapping("/comment/{comment_id}")
    public ResponseEntity<CommentUpdateResponseDto> updateComment(@RequestBody CommentUpdateRequestDto commentUpdateRequestDto,
                                                  @PathVariable Long comment_id,
                                                  @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.updateComment(commentUpdateRequestDto, comment_id, memberDetails);
    }

    @DeleteMapping("/comment/{comment_id}")
    public ResponseEntity<CommentDeleteResponseDto> deleteComment(@PathVariable Long comment_id,
                                                  @AuthenticationPrincipal MemberDetails memberDetails){
        return commentService.deleteComment(comment_id, memberDetails); //.removeCommentById? vs removeComment
    }

    // 관리자, 댓글 삭제
    @Secured(MemberRole.Authority.ADMIN)
    @DeleteMapping("/admin/comment/{commentId}")
    public ResponseEntity<CommentDeleteResponseDto> adminDeletePost(@PathVariable Long commentId,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return commentService.adminDeleteComment(commentId, memberDetails);
    }
}
