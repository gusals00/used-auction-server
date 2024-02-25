package com.auction.usedauction.web.controller;

import com.auction.usedauction.loadTest.InitLoadTest;
import com.auction.usedauction.domain.Category;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.web.dto.LoginReq;
import com.auction.usedauction.web.dto.ResultRes;
import com.opencsv.CSVWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/load")
public class LoadController {

    private final InitLoadTest loadTest;

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

    @Operation(summary = "load 테스트 입찰 데이터 검증")
    @GetMapping("/validation")
    public ResultRes<String> validate() throws IOException {
        boolean validate = loadTest.validate();
        return new ResultRes(validate);
    }
}
