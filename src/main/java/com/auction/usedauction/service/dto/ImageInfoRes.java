package com.auction.usedauction.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ImageInfoRes {
    @Schema(description = "사진 원본 이름", example = "객체지향.jpg")
    private String originalName;
    @Schema(description = "사진 저장 경로", example = "http://sfieuhfe.sfee.jpg")
    private String path;
}
