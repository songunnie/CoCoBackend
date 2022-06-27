package com.igocst.coco.domain.techstack;

import com.igocst.coco.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTechStack {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_TECH_STACK_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TECH_STACK_ID")
    private TechStack techStack;
}
