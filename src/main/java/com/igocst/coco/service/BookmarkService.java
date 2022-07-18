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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<BookmarkSaveResponseDto> join(Long postId, MemberDetails memberDetails) {

        Member member = memberRepository.findByEmail(memberDetails.getMember().getEmail())
                .orElseThrow( () -> new IllegalArgumentException("권한이 없습니다."));

        Post post  = postRepository.findById(postId)
                .orElseThrow( () -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findAllByPostId(postId);

        for(Bookmark b : bookmarks) {

            // 이미 본인이 저장한 북마크는 또 저장할 수 없음
            if (b.getMember().getId() == memberDetails.getMember().getId()) {
//                throw new IllegalArgumentException("북마크에 저장되어있는 게시물입니다.");
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

    @Transactional
    public ResponseEntity<List<BookmarkListReadResponseDto>> readBookmarkList(MemberDetails memberDetails) {

        Member member = memberRepository.findByEmail(memberDetails.getMember().getEmail())
                .orElseThrow( () -> new IllegalArgumentException("권한이 없습니다."));

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


    @Transactional
    public ResponseEntity<BookmarkDeleteResponseDto> deleteBookmark(Long bookmarkId, MemberDetails memberDetails) {

        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow( () -> new IllegalArgumentException("유효한 회원이 아닙니다.")
        );

        boolean isValid = member.deleteBookmark(bookmarkId);

        if(!isValid) {
//            throw new RuntimeException("삭제할 수 없습니다.");
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
