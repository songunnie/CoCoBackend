package com.igocst.coco.controller;

import com.igocst.coco.dto.bookmark.BookmarkDeleteResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkListReadResponseDto;
import com.igocst.coco.dto.bookmark.BookmarkSaveResponseDto;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<BookmarkSaveResponseDto> saveBookmark(@PathVariable Long postId,
                                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        return bookmarkService.saveBookmark(postId, memberDetails);
    }

    @GetMapping("/bookmark/list")
    public ResponseEntity<List<BookmarkListReadResponseDto>> readBookmarkList(@AuthenticationPrincipal MemberDetails memberDetails) {
        return bookmarkService.readBookmarkList(memberDetails);
    }

    @DeleteMapping("/bookmark/{bookmarkId}")
    public ResponseEntity<BookmarkDeleteResponseDto> deleteBookmark(@PathVariable Long bookmarkId,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        return bookmarkService.deleteBookmark(bookmarkId, memberDetails);
    }

}
