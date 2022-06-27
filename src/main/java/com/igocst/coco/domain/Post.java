package com.igocst.coco.domain;

import com.igocst.coco.domain.techstack.PostTechStack;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID") // 외래키와 매핑
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_ID")
    private Region region;

    // 이렇게 컬럼으로 만드는 게 맞는지?
    // 데이터 삽입 시 어떻게?
    // 프로젝트 기간 설정 ex) 1월 1일 ~ 1월 31일
    // LocalDateTime객체 만들어서 넣어주기
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private boolean state;

    @Column(nullable = false)
    private int hits;

    // 좋아요 양방향
    @OneToMany(mappedBy = "post")
    private List<Likes> likes = new ArrayList<>();

    // 북마크 양방향
    @OneToMany(mappedBy = "post")
    private List<Bookmark> bookmarks = new ArrayList<>();

    // 댓글 양방향
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    // 게시글 기술 스택 양방향
    @OneToMany(mappedBy = "post")
    private List<PostTechStack> postTechStacks = new ArrayList<>();
}
