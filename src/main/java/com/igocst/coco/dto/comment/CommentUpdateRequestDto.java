package com.igocst.coco.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CommentUpdateRequestDto {
//    private Long id;
    private String content;
}
