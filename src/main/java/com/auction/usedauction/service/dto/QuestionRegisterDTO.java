package com.auction.usedauction.service.dto;

import com.auction.usedauction.web.dto.QuestionRegisterReq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRegisterDTO {
    private Long parentId; // 부모 댓글 ID
    private String content; // 댓글 내용
    private Long productId; // 상품 ID
    private String memberLoginId; // 댓글 작성자 ID

    public QuestionRegisterDTO(QuestionRegisterReq registerReq,String loginId) {
        this.parentId = registerReq.getParentId();
        this.content = registerReq.getContent();
        this.productId = registerReq.getProductId();
        this.memberLoginId = loginId;
    }
}
