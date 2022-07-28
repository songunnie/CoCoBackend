package com.igocst.coco.dto.member;

import lombok.Getter;

@Getter
public class RegisterRequestDto {
    private String email; // id
    private String password;
    private String nickname;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;
    private String profileImageUrl;
    // 관리자
    private boolean admin = false;
    private String adminToken = "";
}
