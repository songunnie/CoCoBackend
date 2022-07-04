package com.igocst.coco.controller;

import com.igocst.coco.dto.message.*;
import com.igocst.coco.security.MemberDetails;
import com.igocst.coco.security.jwt.JwtTokenProvider;
import com.igocst.coco.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/message")
    public MessageCreateResponseDto create(@RequestBody MessageCreateRequestDto messageCreateRequestDto,
                                           @AuthenticationPrincipal MemberDetails memberDetails) {
        return  messageService.join(messageCreateRequestDto, memberDetails);
    }

    @GetMapping("/message/{messageId}")
    public MessageReadResponseDto readMessage(@PathVariable Long messageId) {
        return messageService.getMessage(messageId);
    }

    @GetMapping("/message/list")
    public List<MessageListReadResponseDto> readMessages(@AuthenticationPrincipal MemberDetails memberDetails) {
        return messageService.getMessageList(memberDetails);
    }

    @DeleteMapping("/message/{messageId}")
    public MessageDeleteResponseDto find(@PathVariable Long messageId) {
        return messageService.deleteMessage(messageId);
    }
}

