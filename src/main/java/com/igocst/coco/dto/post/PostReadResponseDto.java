package com.igocst.coco.dto.post;

import com.igocst.coco.domain.MeetingType;
import com.igocst.coco.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter @Setter
@Builder
public class PostReadResponseDto {
    private String status;
    private Long id;
    private String title;
    private String content;
    private MeetingType meetingType;
    private String contact;
    private String period;
    private boolean recruitmentState;
    private int hits;
    private LocalDateTime postDate;
    private String writer;  // 글 작성자 닉네임
}
