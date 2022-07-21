package com.igocst.coco.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //MySQL에서는 IDENTITY가 먹힌다. (오라클 등에서는 안먹힘!)
    @Column(name = "COMMENT_ID")
    private Long id;

    //FK
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID") //생략가능하지만 안하는게 좋음
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(nullable = false)
    private String content;

    public void registerMember(Member member) {
        this.member = member;
    }

    public void registerPost(Post post) {
        this.post = post;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
