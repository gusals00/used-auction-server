package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.Chat;
import com.auction.usedauction.loadTest.InitLoadTest;
import com.auction.usedauction.domain.Category;
import com.auction.usedauction.loadTest.InitLoadTestForChat;
import com.auction.usedauction.repository.chat.ChatRepository;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.web.dto.LoginReq;
import com.auction.usedauction.web.dto.ResultRes;
import com.opencsv.CSVWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/load")
@Slf4j
public class LoadController {

    private final InitLoadTest loadTest;
    private final InitLoadTestForChat loadTestForChat;
    private final ChatRepository chatRepository;

    @Operation(summary = "load 테스트 데이터 추가")
    @PostMapping("/insert")
    public ResultRes<TokenDTO> login2(@RequestBody @Valid LoginReq loginReq) throws IOException {
        List<Category> categories = loadTest.insertCategory();
        CSVWriter writer = new CSVWriter(new FileWriter("./sample.csv"));
        // 요청 개수 -> memberCnt * loopCnt
        int loopCnt = 90;
        for (int i = 0; i < loopCnt; i++) {
            loadTest.insertData(100, 5, i, categories, writer);
        }
        writer.close();

        return new ResultRes(new TokenDTO());
    }

    @Operation(summary = "load 테스트 채팅 관련 데이터 추가")
    @PostMapping("/insert-chat")
    public ResponseEntity addDataForChat() throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter("./chat.csv"));

        loadTestForChat.insertData(100, 1, writer);

        writer.close();

        return ResponseEntity.ok("ok");
    }

    @Operation(summary = "load 테스트 입찰 데이터 검증")
    @GetMapping("/validation")
    public ResultRes<String> validate() throws IOException {
        boolean validate = loadTest.validate();
        return new ResultRes(validate);
    }
}
