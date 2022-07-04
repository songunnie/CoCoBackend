package com.igocst.coco.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageReadResponseDto {
    private String sender;
    private String title;
    private String content;
    private String status;
}