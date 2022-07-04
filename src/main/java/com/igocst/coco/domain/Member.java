package com.igocst.coco.domain;

import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Getter @Setter
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

    private String profileImageUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String introduction;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    // 게시글 양방향
    @OneToMany(mappedBy = "member")
    @Builder.Default    // 빌더를 클래스레벨에 달아놔서 초기화 위해 필요, 생성자에 빌더를 달면 안써도 됨
    private List<Post> posts = new ArrayList<>();

    // 댓글 양방향
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // 쪽지 양방향
    @OneToMany(mappedBy = "sender")
    @Builder.Default
    private List<Message> sendMessage = new ArrayList<>();

    public void sendMessage(Message message) {
        this.sendMessage.add(message);
        if (message.getSender() != this) {
            message.setSender(this);
        }
    }

    @OneToMany(mappedBy = "receiver")
    @Builder.Default
    private List<Message> readMessage = new ArrayList<>();

    public void readMessage(Message message) {
        this.readMessage.add(message);
        if (message.getReceiver() != this) {
            message.setReceiver(this);
        }
    }

    /**
     * 연관관계 메소드
     *
     */
    // 회원의 게시글 중에서 특정 게시글 삭제
    public boolean deletePost(Long postId) {
        if (postId <= 0) {
            return false;
        }
        // 리스트를 돌아서 해당하는 게시글을 찾는다
        Iterator<Post> iterator = posts.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(postId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // 회원이 작성한 특정 게시글을 찾는다.
    public Post findPost(Long postId) {
        if (postId <= 0) {
            return null;
        }
        Iterator<Post> iterator = posts.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(postId)) {
                return iterator.next();
            }
        }
        return null;
    }
}
