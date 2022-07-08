package com.igocst.coco.repository;

import com.igocst.coco.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 모집 중인 게시글만 가져온다.
    List<Post> findAllByRecruitmentStateFalse();
}
