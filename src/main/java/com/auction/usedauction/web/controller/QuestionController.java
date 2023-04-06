package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.QuestionService;
import com.auction.usedauction.service.dto.QuestionPageContentRes;
import com.auction.usedauction.service.dto.QuestionPageRes;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import com.auction.usedauction.service.query.QuestionQueryService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "댓글 컨트롤러", description = "댓글 관련 api")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionQueryService questionQueryService;

    @Operation(summary = "질문 등록 메서드")
    @PostMapping
    public ResultRes<MessageRes> registerQuestion(@RequestBody @Valid QuestionRegisterReq registerReq, @AuthenticationPrincipal User user) {
        log.info("댓글 작성 컨트롤러");
        System.out.println(registerReq.getProductId() + " " + registerReq.getContent() + " " + registerReq.getParentId());
        QuestionRegisterDTO commentRegister = new QuestionRegisterDTO(registerReq, user.getUsername());
        questionService.registerQuestion(commentRegister);
        return new ResultRes<>(new MessageRes("댓글 등록 성공"));
    }

    @Operation(summary = "질문 리스트 조회 메서드")
    @GetMapping("/{productId}")
    public PageListRes<QuestionPageContentRes> getQuestions(@PathVariable Long productId, @ParameterObject @Valid QuestionSearchReq searchReq) {
        log.info("댓글 조회 컨트롤러");
        PageRequest pageRequest = PageRequest.of(searchReq.getPage(), searchReq.getSize(), Sort.Direction.ASC, "createdDate");
        QuestionPageRes questionPage = questionQueryService.getQuestionPage(pageRequest, productId);
        return new PageListRes<>(questionPage.getQuestionPageContents(),questionPage.getPage());
    }

    @DeleteMapping("/{questionId}")
    public ResultRes<MessageRes> deleteQuestion(@PathVariable Long questionId, @AuthenticationPrincipal User user) {
        log.info("댓글 삭제 컨트롤러");
        questionService.deleteQuestion(questionId, user.getUsername());
        return new ResultRes<>(new MessageRes("댓글 삭제 성공"));

    }
}
