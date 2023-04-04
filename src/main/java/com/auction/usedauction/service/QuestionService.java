package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.QuestionErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.QuestionRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long register(QuestionRegisterDTO registerDTO) {
        // 작성자 존재 여부 확인
        Member findMember = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(registerDTO.getMemberLoginId(), MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.INVALID_USER));

        // 상품 존재 여부 확인
        Product findProduct = productRepository.findByIdAndProductStatusNot(registerDTO.getProductId(), ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 질문 등록
        Question question;
        if (registerDTO.getParentId() == null) {
            question = createQuestion(registerDTO.getContent(), findMember, findProduct, null);
        } else {
            // 부모 조회
            Question findParent = questionRepository.findByIdAndProduct(registerDTO.getParentId(),findProduct)
                    .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

            question = createQuestion(registerDTO.getContent(), findMember, findProduct, findParent);
        }
        questionRepository.save(question);
        return question.getId();
    }

    private Question createQuestion(String content, Member member, Product product, Question parent) {
        return Question.builder().content(content)
                .member(member)
                .product(product)
                .parent(parent)
                .build();
    }
}
