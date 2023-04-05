package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class QuestionPageRes {

    private List<QuestionPageContentRes> questionPageContents;
    private Page<Question> page;

}