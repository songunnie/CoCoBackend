package com.igocst.coco.service;

import com.igocst.coco.domain.Comment;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.comment.*;
import com.igocst.coco.repository.CommentRepository;
import com.igocst.coco.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    //DB동기화를 위해
    @Transactional
    //comment를 받으면 CommentRepository에 저장
    public CommentCreateResponseDto join(CommentCreateRequestDto commentCreateRequestDto, Long post_id) { //join = comment create
        //주인인 Post를 찾고,
        Post post = postRepository.findById(post_id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다")); //주인(Post)을 찾아온것.

        //Comment를 하나 만들고
        Comment comment = Comment.builder()
                .content(commentCreateRequestDto.getContent())
                .build();
        /*주인에게 연관관계 메소드를 통해 "이 댓글 내거야!" 하고 말해줌
        comment는 repo에서 꺼내온게 아니기 때문에 영속성이 없는상태
        post가 영속성이기 때문에 comment도 연관관계 매핑을 통해 영속성으로 들어감*/
        post.addComment(comment);

        //댓글을 repo에 저장해줌. 이거 필요없.
        commentRepository.save(comment);

        return CommentCreateResponseDto.builder()
                .status("댓글이 작성되었습니다")
                //status "메시지가 저장되었습니다" 내려주기
                .build();
    }

    // 댓글 겟 1차시도
    public List<CommentReadResponseDto> readCommentList(Long post_id) {
        //List로 받고
        List<Comment> comments = commentRepository.findAllByPostId(post_id);
        List<CommentReadResponseDto> output = new ArrayList<>();

        for(Comment c : comments) {
            output.add(CommentReadResponseDto.builder()
                    .comments(c.getContent())
                    //c.getPost().getComments는 결국 댓글의 게시글을 불러와서 다시 그 댓글을 다 찍어준 것= 값이 두번씩 찍히는 에러
                    .status("댓글 불러오기 완료")
                    .build());
        }
        return output;
    }

    @Transactional
    public CommentUpdateResponseDto updateComment(CommentUpdateRequestDto commentUpdateRequestDto, Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        //setter를 쓰니까 .setContent로 바로해주면 됨.
        comment.setContent(commentUpdateRequestDto.getContent());

        return CommentUpdateResponseDto.builder()
                .status("댓글이 수정되었습니다")
                .build();
    }

    @Transactional
    public CommentDeleteResponseDto deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다"));

        commentRepository.deleteById(comment.getId());

        return CommentDeleteResponseDto.builder()
                .id(id)
                .build();
    }


}
