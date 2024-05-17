package com.auction.usedauction.repository.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ChatMessageJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

//    public void chatMessageBatchInsert(Set<ChatMessageSaveDTO> chatData) {
//        String sql = "insert into chat_message (created_date, message, read_or_not, chat_room_id, member_id) values(?, ?, ?, ?, ?)";
//
//        jdbcTemplate.batchUpdate(sql,
//                chatData,
//                chatData.size(),
//                (ps, chat) -> {
//            ps.setTimestamp(1, Timestamp.valueOf(chat.getCreatedDate()));
//            ps.setString(2, chat.getMessage());
//            ps.setBoolean(3, chat.isReadOrNot());
//            ps.setLong(4, chat.getRoomId());
//            ps.setLong(5, chat.getMemberId());
//                });
//    }
}
