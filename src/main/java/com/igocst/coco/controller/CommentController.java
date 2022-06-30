package com.igocst.coco.controller;

import com.igocst.coco.domain.Comment;
import com.igocst.coco.dto.*;
import com.igocst.coco.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/comment/{post_id}")
    public CommentCreateResponseDto createComment(@RequestBody CommentCreateRequestDto commentCreateRequestDto,
                                                  @PathVariable Long post_id) {
        return commentService.join(commentCreateRequestDto, post_id);
    }

    @GetMapping("/comment/list/{post_id}")
    public List<CommentReadResponseDto> readComment(@PathVariable Long post_id) {
        return commentService.readCommentList(post_id);
    }

    @PutMapping("/comment/{post_id}")
    public CommentUpdateResponseDto updateComment(@PathVariable Long post_id, @RequestBody CommentUpdateRequestDto commentUpdateRequestDto) {
        return commentService.updateComment(commentUpdateRequestDto, post_id);
    }

    @DeleteMapping("/comment/{comment_id}")
    public CommentDeleteResponseDto deleteComment(@PathVariable Long comment_id){
        return commentService.deleteComment(comment_id); //.removeCommentById? vs removeComment
    }
}
