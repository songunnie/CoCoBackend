package com.igocst.coco.domain;

import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    //Json 타입으로 변환하기 이해 fetch타입 Lazy 해제 후 All로!
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(nullable = false)
    private boolean state;

    @Column(nullable = false)
    private int hits;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeetingType meetingType;

    // 댓글 양방향
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    //주인이 POST
    //Setter를 쓸 때 코드
    //addComment로 이미 양방향 매핑이 되어있어서 changeComment 메소드 안써도됨!
    public void addComment(Comment comment) {
        comment.setPost(this);
        comments.add(comment);
    }
}
