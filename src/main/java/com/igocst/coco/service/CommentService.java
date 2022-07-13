package com.igocst.coco.service;

import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.Comment;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.comment.*;
import com.igocst.coco.repository.CommentRepository;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 댓글 생성
    //DB동기화를 위해
    @Transactional
    //comment를 받으면 CommentRepository에 저장
    public ResponseEntity<CommentCreateResponseDto> join(CommentCreateRequestDto commentCreateRequestDto, Long post_id, MemberDetails memberDetails) { //join = comment create
        //회원을 찾아 가져오고.
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다"));

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
        member.addComment(comment);
        post.addComment(comment);

        //댓글을 repo에 저장해줌. 이거 필요없.
        commentRepository.save(comment);

        return new ResponseEntity<>(
                CommentCreateResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }

    // 댓글 조회
    public ResponseEntity<List<CommentReadResponseDto>> readCommentList(Long post_id, MemberDetails memberDetails) {
        //List로 받고
        List<Comment> comments = commentRepository.findAllByPostId(post_id);
        List<CommentReadResponseDto> output = new ArrayList<>();
        boolean enableDelete;

        for(Comment c : comments) {
            enableDelete = false;

            // 로그인한 회원은 본인이 작성한 댓글만 삭제할 수 있다.
            if (c.getMember().getId() == memberDetails.getMember().getId()) {
                enableDelete = true;
            }

            // 현재 로그인한 회원이 관리자라면 모든 댓글을 삭제할 수 있다.
            MemberRole memberRole = MemberRole.MEMBER;
            if (memberDetails.getMember().getRole().equals(MemberRole.ADMIN)) {
                memberRole = MemberRole.ADMIN;
                enableDelete = true;
            }

            output.add(CommentReadResponseDto.builder()
                    .id(c.getId())
                    .comments(c.getContent())
                    .nickname(c.getMember().getNickname())
                    //c.getPost().getComments는 결국 댓글의 게시글을 불러와서 다시 그 댓글을 다 찍어준 것= 값이 두번씩 찍히는 에러
                    .createDate(c.getCreateDate())
                    .status(StatusMessage.SUCCESS)
                    .enableDelete(enableDelete)
                    .memberRole(memberRole)
                    .build());
        }
        return new ResponseEntity<>(output, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    //댓글 수정
    @Transactional
    public ResponseEntity<CommentUpdateResponseDto> updateComment(CommentUpdateRequestDto commentUpdateRequestDto,
                                                  Long comment_id, MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        Comment comment = member.findComment(comment_id);
        if (comment == null) {
//            throw new RuntimeException("작성한 댓글을 찾을 수 없습니다.");
            return new ResponseEntity<>(
                    CommentUpdateResponseDto.builder()
                            .status(StatusMessage.BAD_REQUEST)
                            .build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }

        //setter를 쓰니까 .setContent로 바로해주면 됨.
        comment.setContent(commentUpdateRequestDto.getContent());

        return new ResponseEntity<>(
                CommentUpdateResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }

    //댓글 삭제
    @Transactional
    public ResponseEntity<CommentDeleteResponseDto> deleteComment(Long id, MemberDetails memberDetails) {
        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));


        boolean isValid = member.deleteComment(id);

        if(!isValid) {
//            throw new RuntimeException("삭제할 수 없습니다.");
            return new ResponseEntity<>(
                    CommentDeleteResponseDto.builder()
                            .status(StatusMessage.BAD_REQUEST)
                            .build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }

        commentRepository.deleteById(id);

        return new ResponseEntity<>(
                CommentDeleteResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }

    // 관리자, 댓글 삭제
    public ResponseEntity<CommentDeleteResponseDto> adminDeleteComment(Long commentId, MemberDetails memberDetails) {
        if (!memberDetails.getMember().getRole().equals(MemberRole.ADMIN)) {
            return new ResponseEntity<>(
                    CommentDeleteResponseDto.builder()
                            .status(StatusMessage.UNAUTHORIZED_USER)
                            .build(),
                    HttpStatus.valueOf(StatusCode.UNAUTHORIZED_USER)
            );
        }
        commentRepository.deleteById(commentId);

        return new ResponseEntity<>(
                CommentDeleteResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }
}
