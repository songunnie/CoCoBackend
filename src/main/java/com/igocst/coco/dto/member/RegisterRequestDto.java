package com.igocst.coco.dto.member;

import lombok.Getter;

@Getter
public class RegisterRequestDto {
    private String email; // id
    private String password;
    private String nickname;
    private String githubUrl;
    private String portfolioUrl;
}
