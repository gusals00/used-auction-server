package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.Question;
import com.auction.usedauction.repository.QuestionRepository;
import com.auction.usedauction.service.dto.QuestionPageContentRes;
import com.auction.usedauction.web.dto.PageListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {
    private final QuestionRepository questionRepository;

    public PageListRes<QuestionPageContentRes> getQuestionPage(Pageable pageable, Long productId) {
        Page<Question> questionPage = questionRepository.findByProduct_IdAndParentIsNull(productId, pageable);
        List<Question> content = questionPage.getContent();

        List<QuestionPageContentRes> questionListContents = content.stream()
                .map(QuestionPageContentRes::new)
                .toList();

        return new PageListRes<>(questionListContents,questionPage);
    }
}
