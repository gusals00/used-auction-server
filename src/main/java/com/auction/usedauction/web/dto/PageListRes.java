package com.auction.usedauction.web.dto;

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

    private int getTotalPages; //전체 페이지 수
    private int getPageNumber; //현재 페이지
    private int getSize; //페이지 크기
    private int getNumberOfElements; //현재 페이지에 나올 데이터 수
    private boolean hasContent; //조회된 데이터 존재 여부
    private boolean isFirst; //현재 페이지가 첫 페이지 인지 여부
    private boolean isLast; //현재 페이지가 마지막 페이지 인지 여부
    private boolean hasNext; //다음 페이지 여부
    private boolean hasPrevious; //이전 페이지 여부

    public PageListRes(List<T> content, Page page) {
        this.content = content;
        this.getTotalPages = page.getTotalPages();
        this.getPageNumber = page.getNumber();
        this.getSize = page.getSize();
        this.getNumberOfElements = page.getNumberOfElements();
        this.hasContent = page.hasContent();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
}
