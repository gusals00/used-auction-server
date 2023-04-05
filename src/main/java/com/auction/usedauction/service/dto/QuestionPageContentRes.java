package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.BaseTimeEntity;
import com.auction.usedauction.domain.Question;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
@NoArgsConstructor
public class QuestionPageContentRes {

    @Schema(description = "댓글 작성자 로그인 아이디",example = "love12422")
    private String loginId;
    @Schema(description = "질문내용",example = "흠집은 없나요")
    private String content;
    @Schema(description = "질문 등록 날짜",example = "2023-04-06 01:05")
    private String createdDate;

    @ArraySchema()
    private List<QuestionPageContentRes> children;

    public QuestionPageContentRes(Question question) {
        this.loginId = question.getMember().getLoginId();
        this.content = question.getContent();
        this.createdDate = question.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // 자식 질문을 날짜순으로 오름차순 정렬
        this.children = question.getChildren().stream()
                .sorted(Comparator.comparing(BaseTimeEntity::getCreatedDate))
                .map(QuestionPageContentRes::new)
                .collect(toList());
    }
}
