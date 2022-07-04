package com.igocst.coco.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageDeleteResponseDto {
    private Long messageId;
    private String status;
}

