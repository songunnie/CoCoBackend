package com.igocst.coco.dto.post;

import com.igocst.coco.domain.MeetingType;
import com.igocst.coco.domain.MemberRole;
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
    // 게시글을 수정,삭제 할 수 있는지 여부 체크
    private boolean enableUpdate;
    private boolean enableDelete;
    // 관리자 여부 체크 (관리자는 모든 게시글 삭제 가능)
    private MemberRole memberRole;
    // 게시글 작성자 정보 불러오기
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;
    private String profileImageUrl;
}
