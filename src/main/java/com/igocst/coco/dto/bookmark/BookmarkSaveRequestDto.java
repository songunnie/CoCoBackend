package com.igocst.coco.dto.bookmark;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkSaveRequestDto {
    private String title;
    private String status;
}
