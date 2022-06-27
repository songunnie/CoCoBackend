package com.igocst.coco.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Region {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REGION_ID")
    private Long id;

    @Column(nullable = false)
    private String name;

    // 게시글 양방향
    @OneToMany(mappedBy = "region")
    private List<Post> posts = new ArrayList<>();

}
