package com.igocst.coco.domain;

import com.igocst.coco.domain.techstack.UserTechStack;
import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String profileImageUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;

    // 계정상태
    // ?
    @Column(nullable = false)
    private int state;

    // 게시글 양방향
    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    // 좋아요 양방향
    @OneToMany(mappedBy = "member")
    private List<Likes> likes = new ArrayList<>();

    // 북마크 양방향
    @OneToMany(mappedBy = "member")
    private List<Bookmark> bookmarks = new ArrayList<>();

    // 댓글 양방향
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    // 회원 기술 스택 양방향
    @OneToMany(mappedBy = "member")
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    // 회원 인증 양방향
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private MemberVerification memberVerification;

    // 쪽지 양방향
    @OneToMany(mappedBy = "sender")
    private List<Message> sendMessage;

    @OneToMany(mappedBy = "receiver")
    private List<Message> receiveMessage;

}
