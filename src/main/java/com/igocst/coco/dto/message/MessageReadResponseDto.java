package com.igocst.coco.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageReadResponseDto {
    private Long id;
    private String member;
    private String sender;
    private String title;
    private String content;
    private String status;
    private boolean readState;
}