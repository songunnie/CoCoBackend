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
    private String content;
}
