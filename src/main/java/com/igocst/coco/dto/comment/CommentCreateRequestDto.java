package com.igocst.coco.dto.comment;

import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.Post;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequestDto {
//    private Long post_id;
    private String content;

//    public static Comment newComment(CommentCreateRequestDto commentCreateRequestDto){
//        Comment comment = new Comment();
//
//        return comment;
//    }

//    @Builder
//    public CommentCreateRequestDto (String content) {
//        this.content = content;
//    }


}
