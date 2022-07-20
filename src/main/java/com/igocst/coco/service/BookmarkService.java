package com.igocst.coco.service;

import com.igocst.coco.common.status.StatusCode;
import com.igocst.coco.common.status.StatusMessage;
import com.igocst.coco.domain.*;
import com.igocst.coco.dto.bookmark.BookmarkDeleteResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkListReadResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkSaveResponseDto;
import com.igocst.coco.repository.BookmarkRepository;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 북마크에 저장하기
    @Transactional
    public ResponseEntity<BookmarkSaveResponseDto> saveBookmark(Long postId, MemberDetails memberDetails) {
        // 영속성이 없는상태
        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        Optional<Post> postOptional  = postRepository.findById(postId);
        if(postOptional.isEmpty()) {
            log.error("nickname={}, messageId={}, error={}", member.getNickname(), postId, "해당 게시글을 찾을 수 없음");
            return new ResponseEntity<>(
                    BookmarkSaveResponseDto.builder()
                            .status(StatusMessage.BAD_REQUEST)
                            .build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST));
        }
        Post post = postOptional.get();

        List<Bookmark> bookmarks = bookmarkRepository.findAllByPostId(postId);

        for(Bookmark b : bookmarks) {

            // 이미 본인이 저장한 북마크는 또 저장할 수 없음
            if (b.getMember().getId() == memberDetails.getMember().getId()) {
                log.error("nickname={}, messageId={}, error={}", member.getNickname(), postId, "이미 북마크에 저장한 게시글");
                return new ResponseEntity<>(
                        BookmarkSaveResponseDto.builder()
                                .status(StatusMessage.BAD_REQUEST)
                                .build(),
                        HttpStatus.valueOf(StatusCode.BAD_REQUEST)
                );
            }
        }

        Bookmark bookmark = new Bookmark();

        member.addBookmark(bookmark);
        post.addBookmark(bookmark);

        bookmarkRepository.save(bookmark);

        return new ResponseEntity<>(
                BookmarkSaveResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }

    // 북마크 리스트 불러오기
    @Transactional
    public ResponseEntity<List<BookmarkListReadResponseDto>> readBookmarkList(MemberDetails memberDetails) {

        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        List<Bookmark> bookmarks = member.getBookmarks();

        List<BookmarkListReadResponseDto> bookmarkList = new ArrayList<>();
        for (Bookmark b : bookmarks) {
            bookmarkList.add(BookmarkListReadResponseDto.builder()
                    .id(b.getId())
                    .postId(b.getPost().getId())
                    .title(b.getPost().getTitle())
                    .meetingType(b.getPost().getMeetingType())
                    .period(b.getPost().getPeriod())
                    .recruitmentState(b.getPost().isRecruitmentState())
                    .hits(b.getPost().getHits())
                    .status(StatusMessage.SUCCESS)
                    .build());
        }
        return new ResponseEntity<>(bookmarkList, HttpStatus.valueOf(StatusCode.SUCCESS));
    }


    // 북마크 삭제
    @Transactional
    public ResponseEntity<BookmarkDeleteResponseDto> deleteBookmark(Long bookmarkId, MemberDetails memberDetails) {

        Optional<Member> memberOptional = memberRepository.findById(memberDetails.getMember().getId());
        Member member = memberOptional.get();

        boolean isValid = member.deleteBookmark(bookmarkId);

        if(!isValid) {
            log.error("nickname={}, messageId={}, error={}", member.getNickname(), bookmarkId, "해당 북마크를 찾을 수 없음");
            return new ResponseEntity<>(
                    BookmarkDeleteResponseDto.builder()
                            .status(StatusMessage.BAD_REQUEST)
                            .build(),
                    HttpStatus.valueOf(StatusCode.BAD_REQUEST)
            );
        }

        bookmarkRepository.deleteById(bookmarkId);

        return new ResponseEntity<>(
                BookmarkDeleteResponseDto.builder()
                        .status(StatusMessage.SUCCESS)
                        .build(),
                HttpStatus.valueOf(StatusCode.SUCCESS)
        );

    }

}
