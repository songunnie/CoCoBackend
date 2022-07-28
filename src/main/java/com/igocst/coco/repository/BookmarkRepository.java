package com.igocst.coco.repository;

import com.igocst.coco.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByPostId(Long postId);
}
