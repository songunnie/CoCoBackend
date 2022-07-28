package com.igocst.coco.dto.post;

import com.igocst.coco.domain.MeetingType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostSaveRequestDto {
    private String title;
    private String content;
    private MeetingType meetingType;
    private String contact;
    private String period;
}
