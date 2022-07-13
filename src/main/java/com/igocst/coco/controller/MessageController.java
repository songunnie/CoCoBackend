package com.igocst.coco.controller;

import com.igocst.coco.dto.message.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/message")
    public ResponseEntity<MessageCreateResponseDto> create(@RequestBody MessageCreateRequestDto messageCreateRequestDto,
                                                          @AuthenticationPrincipal MemberDetails memberDetails) {
        return  messageService.join(messageCreateRequestDto, memberDetails);
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<MessageReadResponseDto> readMessage(@PathVariable Long messageId,
                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        return messageService.getMessage(messageId, memberDetails);
    }

    @GetMapping("/message/list")
    public ResponseEntity<List<MessageListReadResponseDto>> readMessages(@AuthenticationPrincipal MemberDetails memberDetails) {
        return messageService.getMessageList(memberDetails);
    }

    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<MessageDeleteResponseDto> find(@PathVariable Long messageId,
                                         @AuthenticationPrincipal MemberDetails memberDetails) {
        return messageService.deleteMessage(messageId, memberDetails);
    }
}

