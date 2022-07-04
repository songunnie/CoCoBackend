package com.igocst.coco.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.*;

import javax.persistence.*;

@Entity
@Setter
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

    //Comment에서 하면 동작은 하지만 두번 타는 코드를 짜게된다.
//    public void changePost(Post post) {
//        this.post = post;
//        post.getComments().add(this);
//    }
}

// 단방향
// 의도치 않은 테이블 생성
// 쿼리가 두번 날라간다 insert, update

// 양방향
// 객체지향적으로 좋진 않지만 쿼리가 두번 나갈바엔 이게 낫다