package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.BaseTimeEntity;
import com.auction.usedauction.domain.Question;
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
    private String loginId;
    private String content;
    private String createdDate;
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
