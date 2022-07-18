package com.igocst.coco.repository;

import com.igocst.coco.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    //List로 받아오는 메소드를 하나 더 만들기
    List<Comment> findAllByPostId(Long post_id);

    List<Comment> findAllByMember_Id(Long id);
}
