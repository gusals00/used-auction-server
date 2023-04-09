package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.domain.file.QuestionStatus;
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
    public Long registerQuestion(QuestionRegisterDTO registerDTO) {
        // 작성자 존재 여부 확인
        Member findMember = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(registerDTO.getMemberLoginId(), MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 상품 존재 여부 확인
        Product findProduct = productRepository.findByIdAndProductStatusNot(registerDTO.getProductId(), ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 질문 등록
        return insertQuestion(registerDTO, findMember, findProduct);
    }

    @Transactional
    public Long deleteQuestion(Long questionId, String loginId) {
        Question findQuestion = questionRepository.findByIdAndStatus(questionId, QuestionStatus.EXIST)
                .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        // 질문 작성자인지 확인
        if (!findQuestion.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        findQuestion.changeStatus(QuestionStatus.DELETED);
        return findQuestion.getId();
    }

    private Long insertQuestion(QuestionRegisterDTO registerDTO, Member findMember, Product findProduct) {
        Question question;

        if (registerDTO.getParentId() == null) {
            question = createQuestion(registerDTO.getContent(), findMember, findProduct, null, 0);
        } else {
            // 부모 조회
            Question findParent = questionRepository.findByIdAndProduct(registerDTO.getParentId(), findProduct)
                    .orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

            //작성 댓글이 대댓글인지 확인 (대댓글까지만 작성 가능)
            if (findParent.getLayer() > 0) {
                throw new CustomException(QuestionErrorCode.INVALID_LAYER_QUESTION);
            }

            //대댓글 작성자가 판매자가 아닌 경우
            if (!findProduct.getMember().getLoginId().equals(registerDTO.getMemberLoginId())) {
                throw new CustomException(QuestionErrorCode.QUESTION_WRITE_SELLER_ONLY);
            }
            question = createQuestion(registerDTO.getContent(), findMember, findProduct, findParent, findParent.getLayer() + 1);
        }
        questionRepository.save(question);
        return question.getId();

    }


    private Question createQuestion(String content, Member member, Product product, Question parent, int layer) {
        return Question.builder().content(content)
                .member(member)
                .product(product)
                .parent(parent)
                .layer(layer)
                .build();
    }
}
