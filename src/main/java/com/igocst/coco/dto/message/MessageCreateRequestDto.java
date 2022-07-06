package com.igocst.coco.dto.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageCreateRequestDto {
    private String receiver;
    private String title;
    private String content;
    private LocalDateTime createDate = LocalDateTime.now();
}