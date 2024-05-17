package com.auction.usedauction.loadTest;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.chat.ChatRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.UploadFileDTO;
import com.opencsv.CSVWriter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile(value = {"local", "production"})
public class InitLoadTestForChat {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final EntityManager em;
    private final AuthorityRepository authorityRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    public void insertData(int chatRoomCnt, int loopCnt, CSVWriter writer) {
        Authority authority = createAuthority("ROLE_USER");

        for(int i=0; i<chatRoomCnt*2; i+=2) {
            Member seller = createMember("김" + (loopCnt * chatRoomCnt + i + 1), "990828", "email" + (i + 1) + "@kumoh.ac.kr", "hyeonmin" + (loopCnt * chatRoomCnt + i + 1), "password", "010-1234-5678", authority);
            Member buyer = createMember("김" + (loopCnt * chatRoomCnt + i + 2), "990828", "email" + (i + 2) + "@kumoh.ac.kr", "hyeonmin" + (loopCnt * chatRoomCnt + i + 2), "password", "010-1234-5678", authority);
            memberRepository.save(seller);
            memberRepository.save(buyer);

            Product product = Product.builder()
                    .name("상품" + (loopCnt * chatRoomCnt + i + 1))
                    .info("팝니다.")
                    .member(seller)
                    .build();
            em.persist(product);

            ChatRoom chatRoom = ChatRoom.builder()
                    .member(buyer)
                    .product(product)
                    .build();
            chatRoomRepository.save(chatRoom);

            List<Chat> messages = new ArrayList<>();
            for(int message=0; message<1000; message++) {
                Chat chat = Chat.builder()
                        .createdDate(LocalDateTime.now())
                        .message("테스트 메세지")
                        .sender(buyer.getName())
                        .chatRoomId(chatRoom.getId())
                        .build();
                messages.add(chat);
            }
            chatRepository.saveAll(messages);

            TokenDTO buyerToken = memberService.login(buyer.getLoginId(), "password");
            TokenDTO sellerToken = memberService.login(seller.getLoginId(), "password");

            writer.writeNext(new String[]{String.valueOf(chatRoom.getId()), sellerToken.getAccessToken(), seller.getLoginId()});
            writer.writeNext(new String[]{String.valueOf(chatRoom.getId()), buyerToken.getAccessToken(), buyer.getLoginId()});
        }
    }

    private Authority createAuthority(String name) {
        Optional<Authority> roleUser = authorityRepository.findById(name);
        if (roleUser.isEmpty()) {
            Authority authority = Authority.builder()
                    .authorityName(name)
                    .build();
            em.persist(authority);
            return authority;
        }
        return roleUser.get();
    }

    private Member createMember(String name, String birth, String email, String loginId, String password, String phoneNumber, Authority authorities) {
        return Member.builder()
                .name(name)
                .birth(birth)
                .email(email)
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .authorities(Collections.singleton(authorities))
                .build();
    }
}
