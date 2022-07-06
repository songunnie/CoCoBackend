package com.igocst.coco.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {
    // TODO: password 검사하고 수정 가능하도록, imageUrl도 넣어줘야함.
    //private String password;
    //private String imageUrl
    private String nickname;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;
}