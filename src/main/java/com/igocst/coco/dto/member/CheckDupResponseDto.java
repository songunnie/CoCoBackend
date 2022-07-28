package com.igocst.coco.dto.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckDupResponseDto {
    private String status;
    private boolean isDup;
}
