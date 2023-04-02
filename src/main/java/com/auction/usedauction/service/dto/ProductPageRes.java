package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductPageRes {

    private List<ProductPageContentRes> productPageContents;
    private Page<Product> page;

}
