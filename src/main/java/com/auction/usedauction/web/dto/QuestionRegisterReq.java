package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "부모 댓글 ID, 없으면 NULL",example = "1")

    private Long parentId; // 부모 댓글 ID

    @NotEmpty
    @Schema(description = "댓글 내용",example = "낙서 있나요")
    private String content; // 댓글 내용
    @NotNull
    @Schema(description = "상품 ID",example = "1")
    private Long productId; // 상품 ID
}
