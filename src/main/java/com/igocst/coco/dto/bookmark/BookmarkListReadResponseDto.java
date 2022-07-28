package com.igocst.coco.dto.bookmark;

import com.igocst.coco.domain.MeetingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookmarkListReadResponseDto {
    private String status;
    private Long id;
    private Long postId;
    private String title;
    private MeetingType meetingType;
    private String period;
    private boolean recruitmentState;
    private int hits;
}
