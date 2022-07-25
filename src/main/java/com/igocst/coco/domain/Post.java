package com.igocst.coco.domain;

import com.igocst.coco.domain.timestamped.Timestamped;
import com.igocst.coco.dto.post.PostUpdateRequestDto;
import com.nhncorp.lucy.security.xss.XssPreventer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long id;

    // JSON으로 타입으로 변환하기 위해 fetch타입 LAZY를 해제
    @ManyToOne()
    @JoinColumn(name = "MEMBER_ID") // 외래키와 매핑
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private boolean recruitmentState;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int hits;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeetingType meetingType;

    // 댓글 양방향, 게시글이 삭제되면, 댓글도 같이 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    // 북마크
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks = new ArrayList<>();

    /*
     * 비즈니스 로직
     * 게시글 수정 */
    public void updatePost(PostUpdateRequestDto requestDto) {
        this.title = XssPreventer.escape(requestDto.getTitle());
        this.content = XssPreventer.escape(requestDto.getContent());
        this.recruitmentState = requestDto.isRecruitmentState();
        this.meetingType = requestDto.getMeetingType();
        this.period = requestDto.getPeriod();
        this.contact = requestDto.getContact();
    }

    //댓글 - 주인이 POST
    public void createComment(Comment comment) {
        comment.registerPost(this);
        comments.add(comment);
    }

    // 북마크
    public void addBookmark(Bookmark bookmark) {
        bookmark.registerPost(this);
        bookmarks.add(bookmark);
    }

    // 게시글 작성한 회원
    public void registerMember(Member member) { this.member = member; }
}
