package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.ChatMessage;
import com.auction.usedauction.repository.chat.ChatMessageRepository;
import com.auction.usedauction.service.dto.ChatMessageRes;
import com.auction.usedauction.web.dto.PageListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatMessageQueryService {

    private final ChatMessageRepository chatMessageRepository;

    public PageListRes<ChatMessageRes> getMessageList(Pageable pageable, Long roomId, String loginId) {
        Page<ChatMessage> findPage = chatMessageRepository.findByChatRoomIdOrderByCreatedDateDesc(roomId, pageable);

        List<ChatMessageRes> contents = findPage.stream()
                .map(chatMessage -> new ChatMessageRes(chatMessage, loginId))
                .toList();

        return new PageListRes(contents, findPage);
    }
}
