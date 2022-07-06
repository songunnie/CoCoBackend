package com.igocst.coco.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberReadResponseDto {
    private String email;
    private String nickname;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;
    private String status;
}
