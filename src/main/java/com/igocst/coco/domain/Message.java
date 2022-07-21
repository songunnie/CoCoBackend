package com.igocst.coco.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long id;

    // 쪽지 보내는 회원
    @ManyToOne(fetch = FetchType.LAZY)
    private Member sender;

    // 쪽지 받는 회원
    @ManyToOne(fetch = FetchType.LAZY)
    private Member receiver;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean readState;

    @Column(nullable = false)
    private LocalDateTime createDate;

    public void sendMember(Member member) { this.sender = member;}
    public void changeReadState() { this.readState = true; }
}
