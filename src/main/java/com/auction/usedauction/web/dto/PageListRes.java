package com.auction.usedauction.web.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageListRes<T> {

    private List<T> content; // 데이터

    @Schema(description = "전체 페이지 수",example = "10")
    private int totalPages; //전체 페이지

    @Schema(description = "현재 페이지",example = "0")
    private int pageNumber; //현재 페이지

    @Schema(description = "페이지 크기",example = "10")
    private int size; //페이지 크기

    @Schema(description = "현재 페이지에 나올 데이터 수",example = "10")
    private int numberOfElements; //현재 페이지에 나올 데이터 수

    @Schema(description = "조회된 데이터 존재 여부",example = "10")
    private boolean hasContent; //조회된 데이터 존재 여부

    @Schema(description = "현재 페이지가 첫 페이지 인지 여부",example = "true")
    private boolean isFirst; //현재 페이지가 첫 페이지 인지 여부

    @Schema(description = "현재 페이지가 마지막 페이지 인지 여부",example = "false")
    private boolean isLast; //현재 페이지가 마지막 페이지 인지 여부

    @Schema(description = "다음 페이지 여부",example = "true")
    private boolean hasNext; //다음 페이지 여부

    @Schema(description = "이전 페이지 여부",example = "false")
    private boolean hasPrevious; //이전 페이지 여부

    @Schema(description = "전체 데이터 수", example = "100")
    private Long totalElements; // 전체 데이터 수

    public PageListRes(List<T> content, Page page) {
        this.content = content;
        this.totalPages = page.getTotalPages();
        this.pageNumber = page.getNumber();
        this.size = page.getSize();
        this.numberOfElements = page.getNumberOfElements();
        this.hasContent = page.hasContent();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.totalElements = page.getTotalElements();
    }
}
