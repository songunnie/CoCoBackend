package com.igocst.coco.domain;

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

    // 게시글 양방향
    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    // 댓글 양방향
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    // 쪽지 양방향
    @OneToMany(mappedBy = "sender")
    private List<Message> sendMessage = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")
    private List<Message> receiveMessage = new ArrayList<>();
}
