package com.igocst.coco.service;

import com.igocst.coco.domain.Member;
import com.igocst.coco.domain.Message;
import com.igocst.coco.dto.message.*;
import com.igocst.coco.repository.MemberRepository;
import com.igocst.coco.repository.MessageRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    // 쪽지 보내기
    @Builder
    @Transactional
    public MessageCreateResponseDto join(MessageCreateRequestDto messageCreateRequestDto) {

        // 샘플(sender)
        Member member = Member.builder()
                .email("seri@naver.com")
                .nickname("seri")
                .password("1234")
                .profileImageUrl("lsdkfsld")
                .build();

        String receiver = messageCreateRequestDto.getReceiver();
        Member receivedMember = memberRepository.findByEmail(receiver)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람이 존재하지 않습니다."));


        // message 보내는 코드
        Message message = Message.builder()
                .receiver(receivedMember)
                .title(messageCreateRequestDto.getTitle())
                .content(messageCreateRequestDto.getContent())
                .build();

        member.sendMessage(message);
        messageRepository.save(message);

        return MessageCreateResponseDto.builder()
                .status("쪽지 전송에 성공했습니다.")
                .build();
    }


    // 쪽지 상세 읽기
    @Transactional
    public MessageReadResponseDto getMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지가 존재하지 않습니다."));

        return MessageReadResponseDto.builder()
                .sender(message.getSender().getEmail())
                .title(message.getTitle())
                .content(message.getContent())
                .status("쪽지를 불러오는데 성공했습니다.")
                .build();
    }


    // 쪽지 리스트 읽기
    @Transactional
    public List<MessageListReadResponseDto> getMessageList() {
        Member member = memberRepository.findById(1L)  // 1번은 샘플
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        List<Message> messages = member.getReadMessage();

        List<MessageListReadResponseDto> messageList = new ArrayList<>();
        for (Message m : messages) {
            messageList.add(MessageListReadResponseDto.builder()
                    .title(m.getTitle())
                    .sender(m.getSender().getEmail())
                    .status("쪽지 리스트를 불러오는데 성공했습니다.")
                    .build());
        }
        return messageList;
    }


    // 쪽지 삭제
    @Transactional
    public MessageDeleteResponseDto deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지가 존재하지 않습니다."));

        messageRepository.delete(message);

        return MessageDeleteResponseDto.builder()
                .messageId(messageId)
                .status("쪽지를 삭제하는데 성공했습니다.")
                .build();
    }
}