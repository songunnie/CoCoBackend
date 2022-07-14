package com.igocst.coco.domain;

import com.igocst.coco.domain.timestamped.Timestamped;
import com.igocst.coco.dto.post.PostUpdateRequestDto;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long id;

    // JSON으로 타입으로 변환하기 위해 fetch타입 LAZY를 해제
    // cascade = CascadeType.PERSIST 게시글 생성중 잠시 주석처리
    @ManyToOne()
    @JoinColumn(name = "MEMBER_ID") // 외래키와 매핑
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private String contact;

    // TODO: 모집 완료 = true, 모집 중 = false ?
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

    /**
     * 연관관계 편의 메소드
     */
    // 게시글
    // 어떤 회원의 게시글인지
    public void addMember(Member member) {
        this.member = member;
        member.getPosts().add(this);
    }

    /**
     * 비즈니스 로직
     * @param requestDto
     * 게시글 수정
     */
    public void updatePost(PostUpdateRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
        this.recruitmentState = requestDto.isRecruitmentState();
        this.meetingType = requestDto.getMeetingType();
        this.period = requestDto.getPeriod();
        this.contact = requestDto.getContact();
    }

    //댓글의 주인이 POST
    //Setter를 쓸 때 코드
    //addComment로 이미 양방향 매핑이 되어있어서 changeComment 메소드 안써도됨!
    public void addComment(Comment comment) {
        comment.setPost(this);
        comments.add(comment);
    }

    // 북마크
    public void addBookmark(Bookmark bookmark) {
        bookmark.setPost(this);
        bookmarks.add(bookmark);
    }
}
