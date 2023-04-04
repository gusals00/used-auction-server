package com.auction.usedauction.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRegisterReq {

    private Long parentId; // 부모 댓글 ID
    @NotEmpty
    private String content; // 댓글 내용
    @NotNull
    private Long productId; // 상품 ID
}
