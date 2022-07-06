package com.igocst.coco.domain;

import com.igocst.coco.domain.timestamped.Timestamped;
import lombok.*;
import org.hibernate.annotations.common.reflection.XMember;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // 게시글 양방향, 회원이 삭제되면, 게시글도 같이 삭제
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default    // 빌더를 클래스레벨에 달아놔서 초기화 위해 필요, 생성자에 빌더를 달면 안써도 됨
    private List<Post> posts = new ArrayList<>();

    // 댓글 양방향, 회원이 삭제되면, 댓글도 같이 삭제
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // 쪽지 양방향, 회원이 삭제되면, 쪽지(발송한 쪽지)도 같이 삭제
    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Message> sendMessage = new ArrayList<>();

    public void sendMessage(Message message) {
        this.sendMessage.add(message);
        if (message.getSender() != this) {
            message.setSender(this);
        }
    }

    // 회원이 삭제되면, 쪽지(수신한 쪽지)도 같이 삭제
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
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
     */
    // 회원이 작성한 게시글 추가
    public void addPost(Post post) {
        post.setMember(this);
        posts.add(post);
    }


    // 회원의 게시글 중에서 특정 게시글 삭제
    public boolean deletePost(Long postId) {
        if (postId <= 0) {
            return false;
        }
        // 리스트를 돌아서 해당하는 게시글을 찾는다
//        Iterator<Post> iterator = posts.iterator();
//        while (iterator.hasNext()) {
//            if (iterator.next().getId().equals(postId)) {
//                iterator.remove();
//                return true;
//            }
//        }
//        return false;
        for (Post post : posts) {
            if (post.getId() == postId){
                posts.remove(post);
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
        for (Post post : posts) {
            if (post.getId() == postId) {
                return post;
            }
        }
        return null;
    }

    // 연관관계 메소드 / 댓글 - 회원
    // member와 comment의 연관관계 메소드
    public void addComment(Comment comment) {
        comment.setMember(this);
        comments.add(comment);
    }

    //회원이 작성한 댓글 찾기
    public Comment findComment(Long commentId) {
        if (commentId <= 0) {
            return null;
        }
        for (Comment comment : comments) {
            if (comment.getId() == commentId) {
                return comment;
            }
        }
        return null;
    }

    // 회원이 받은 쪽지를 찾는다.
    public Message findMessage(Long messageId) {
        if (messageId <= 0) {
            return null;
        }
        for (Message message : readMessage) {
            if (message.getId() == messageId) {
                return message;
            }
        }
        return null;
    }

    // 회원이 받은 쪽지를 삭제한다.
    public boolean deleteMessage(Long messageId) {
        if (messageId <= 0) {
            return false;
        }
        // 리스트를 돌아서 해당하는 쪽지 찾는다
        Iterator<Message> iterator = readMessage.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(messageId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean deleteComment(Long commentId) {
        if (commentId <= 0) {
            return false;
        }
        // 리스트를 돌아서 해당하는 게시글을 찾는다
        Iterator<Comment> iterator = comments.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(commentId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // 회원정보 수정
    public void updateNickname(String nickname){ this.nickname = nickname; }

    public void updateGithubUrl(String githubUrl){
        this.githubUrl = githubUrl;
    }

    public void updatePortfolioUrl(String portfolioUrl){
        this.portfolioUrl = portfolioUrl;
    }

    public void updateIntroduction(String introduction){
        this.introduction = introduction;
    }
}
