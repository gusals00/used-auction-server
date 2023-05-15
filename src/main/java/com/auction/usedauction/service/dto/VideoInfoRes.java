package com.auction.usedauction.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class VideoInfoRes {

    @Schema(description = "동영상 원본 이름", example = "1.mp4")
    private String originalName;
    @Schema(description = "동영상 저장 경로", example = "http://sfieuhfe.1.mp4")
    private String path;
}
