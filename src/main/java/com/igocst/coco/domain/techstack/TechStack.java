package com.igocst.coco.domain.techstack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TECH_STACK_ID")
    private Long id;

    @Column(nullable = false)
    private String name;

    // 왜 int?
    @Column(nullable = false)
    private int type;

    // 게시글 기술 스택 양방향
    @OneToMany(mappedBy = "techStack")
    private List<PostTechStack> postTechStacks = new ArrayList<>();

    // 회원 기술 스택 양방향
    @OneToMany(mappedBy = "techStack")
    private List<UserTechStack> userTechStacks = new ArrayList<>();
}
