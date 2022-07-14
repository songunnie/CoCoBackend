package com.igocst.coco.service;

import com.igocst.coco.domain.*;
import com.igocst.coco.dto.bookmark.BookmarkDeleteResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkListReadResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkSaveResponseDto;
import com.igocst.coco.repository.BookmarkRepository;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.PostRepository;
import com.igocst.coco.security.MemberDetails;
import lombok.RequiredArgsConstructor;
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
    public BookmarkSaveResponseDto join(Long postId, MemberDetails memberDetails) {

        Member member = memberRepository.findByEmail(memberDetails.getMember().getEmail())
                .orElseThrow( () -> new IllegalArgumentException("권한이 없습니다."));

        Post post  = postRepository.findById(postId)
                .orElseThrow( () -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findAllByPostId(postId);

        for(Bookmark b : bookmarks) {

            // 이미 본인이 저장한 북마크는 또 저장할 수 없음
            if (b.getMember().getId() == memberDetails.getMember().getId()) {
                throw new IllegalArgumentException("북마크에 저장되어있는 게시물입니다.");
            }
        }

        Bookmark bookmark = Bookmark.builder()
                .id(post.getId())
                .build();

        member.addBookmark(bookmark);
        post.addBookmark(bookmark);

        bookmarkRepository.save(bookmark);

        return BookmarkSaveResponseDto.builder()
                .status("200")
                .build();
    }

    @Transactional
    public List<BookmarkListReadResponseDto> readBookmarkList(MemberDetails memberDetails) {

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
                    .status("쪽지 리스트를 불러오는데 성공했습니다.")
                    .build());

        }
        return bookmarkList;
    }


    @Transactional
    public BookmarkDeleteResponseDto deleteBookmark(Long bookmarkId, MemberDetails memberDetails) {

        Member member = memberRepository.findById(memberDetails.getMember().getId())
                .orElseThrow( () -> new IllegalArgumentException("유효한 회원이 아닙니다.")
        );

        boolean isValid = member.deleteBookmark(bookmarkId);

        if(!isValid) {
            throw new RuntimeException("삭제할 수 없습니다.");
        }

        bookmarkRepository.deleteById(bookmarkId);

        return BookmarkDeleteResponseDto.builder()
                .status("200")
                .build();
    }

}
