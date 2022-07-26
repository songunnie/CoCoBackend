package com.igocst.coco.service;

import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.Comment;
import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.MemberRole;
import com.igocst.coco.domain.Post;
import com.igocst.coco.dto.comment.*;
import com.igocst.coco.dto.post.PostReadResponseDto;
import com.igocst.coco.repository.CommentRepository;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import com.nhncorp.lucy.security.xss.XssPreventer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 댓글 생성
    // DB동기화를 위해 Transactional 어노테이션 사용
    @Transactional
    public ResponseEntity<CommentCreateResponseDto> createComment(CommentCreateRequestDto commentCreateRequestDto, Long postId, MemberDetails memberDetails) { //join = comment create
        //회원을 찾아 가져오고.
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        //주인인 Post를 찾고,
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            log.error("nickname={}, error={}", member.getNickname(), "해당 게시글을 찾을 수 없음");
            return new ResponseEntity<>(
                    CommentCreateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Post post = postOptional.get();

        if (commentCreateRequestDto.getContent().length() > 255) {
            log.error("nickname={}, error={}", member.getNickname(), "댓글 글자 255자 초과");
            return new ResponseEntity<>(
                    CommentCreateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        //Comment를 하나 만들고
        Comment comment = Comment.builder()
                .content(XssPreventer.escape(commentCreateRequestDto.getContent()))
                .build();
        /*주인에게 연관관계 메소드를 통해 "이 댓글 내거야!" 하고 말해줌
        comment는 repo에서 꺼내온게 아니기 때문에 영속성이 없는상태
        post가 영속성이기 때문에 comment도 연관관계 매핑을 통해 영속성으로 들어감*/
        member.createComment(comment);
        post.createComment(comment);

        //댓글을 repo에 저장해줌. 이거 필요없.
        commentRepository.save(comment);

        return new ResponseEntity<>(
                CommentCreateResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 댓글 조회
    public ResponseEntity<List<CommentReadResponseDto>> readCommentList(Long postId, MemberDetails memberDetails) {
        //List로 받고
        List<Comment> comments = commentRepository.findAllByPostId(postId);
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
                    .profileImageUrl(c.getMember().getProfileImageUrl())
                    .createDate(c.getCreateDate())
                    .modifyDate(c.getLastModifiedDate())
                    .status(StatusMessage.SUCCESS)
                    .enableDelete(enableDelete)
                    .memberRole(memberRole)
                    .build());
        }
        return new ResponseEntity<>(output, HttpStatus.valueOf(StatusCode.SUCCESS));
    }

    // 댓글 하나 조회
    public ResponseEntity<CommentReadResponseDto> readCommentDetail(Long commentId, MemberDetails memberDetails) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        Comment findComment = commentOptional.get();

        return new ResponseEntity<>(
                CommentReadResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .id(findComment.getId())
                        .nickname(findComment.getMember().getNickname())
                        .portfolioUrl(findComment.getMember().getPortfolioUrl())
                        .githubUrl(findComment.getMember().getGithubUrl())
                        .introduction(findComment.getMember().getIntroduction())
                        .profileImageUrl(findComment.getMember().getProfileImageUrl())
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }
    //댓글 수정
    @Transactional
    public ResponseEntity<CommentUpdateResponseDto> updateComment(CommentUpdateRequestDto commentUpdateRequestDto,
                                                  Long commentId, MemberDetails memberDetails) {
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        Optional<Comment> commentOptional = member.findComment(commentId);
        if (commentOptional.isEmpty()) {
            log.error("nickname={}, error={}", member.getNickname(), "해당 댓글을 찾을 수 없습니다.");
            return new ResponseEntity<>(
                    CommentUpdateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }

        if (commentUpdateRequestDto.getContent().length() > 255) {
            log.error("nickname={}, error={}", member.getNickname(), "댓글 수정 글자 255자 초과");
            return new ResponseEntity<>(
                    CommentUpdateResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        Comment comment = commentOptional.get();
        comment.updateContent(commentUpdateRequestDto.getContent());

        return new ResponseEntity<>(
                CommentUpdateResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    //댓글 삭제
    @Transactional
    public ResponseEntity<CommentDeleteResponseDto> deleteComment(Long id, MemberDetails memberDetails) {
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        boolean isValid = member.deleteComment(id);
        if(!isValid) {
            log.error("nickname={}, error={}", member.getNickname(), "댓글을 찾을 수 없습니다.");
            return new ResponseEntity<>(
                    CommentDeleteResponseDto.builder().status(StatusMessage.BAD_REQUEST).build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }
        commentRepository.deleteById(id);

        return new ResponseEntity<>(
                CommentDeleteResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }

    // 관리자, 댓글 삭제
    public ResponseEntity<CommentDeleteResponseDto> adminDeleteComment(Long commentId, MemberDetails memberDetails) {
        if (!memberDetails.getMember().getRole().equals(MemberRole.ADMIN)) {
            log.error("nickname={}, error={}", memberDetails.getNickname(), "관리자 권한이 없음");
            return new ResponseEntity<>(
                    CommentDeleteResponseDto.builder().status(StatusMessage.FORBIDDEN_USER).build(),
                    HttpStatus.valueOf(StatusCode.FORBIDDEN_USER)
            );
        }
        commentRepository.deleteById(commentId);

        return new ResponseEntity<>(
                CommentDeleteResponseDto.builder().status(StatusMessage.SUCCESS).build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );
    }
}
