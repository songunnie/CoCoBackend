package com.igocst.coco.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {
    // TODO: password 검사하고 수정 가능하도록, imageUrl도 넣어줘야함.
    //private String password;
    private String nickname;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;
    //프로필 페이지 이미지
    private MultipartFile file;
}