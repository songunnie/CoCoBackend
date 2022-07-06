package com.igocst.coco.dto.message;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageListReadResponseDto {
    private Long id;
    private String title;
    private String sender;
    private Boolean readState;
    private LocalDateTime createDate;
    private String status;
}