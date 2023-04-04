package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.QuestionService;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import com.auction.usedauction.web.dto.QuestionRegisterReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "댓글 컨트롤러", description = "댓글 관련 api")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResultRes<MessageRes> registerComment(@RequestBody @Valid QuestionRegisterReq registerReq, @AuthenticationPrincipal User user) {
        log.info("댓글 작성 컨트롤러");
        System.out.println(registerReq.getProductId()+" "+registerReq.getContent()+" "+registerReq.getParentId());
        QuestionRegisterDTO commentRegister = new QuestionRegisterDTO(registerReq,user.getUsername());
        questionService.register(commentRegister);
        return new ResultRes<>(new MessageRes("댓글 등록 성공"));
    }
}
