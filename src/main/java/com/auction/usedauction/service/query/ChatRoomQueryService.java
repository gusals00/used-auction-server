package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.ChatRoom;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.web.dto.ChatRoomRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public List<ChatRoomRes> getRoomLists(String loginId) {
        Member member = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByMemberId(member.getId());

        return chatRooms.stream()
                .map(chatRoom -> new ChatRoomRes(chatRoom, member.getId()))
                .collect(Collectors.toList());
    }

}
