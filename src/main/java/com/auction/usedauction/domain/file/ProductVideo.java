package com.auction.usedauction.domain.file;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVideo extends File {


    @Builder
    public ProductVideo(String fullPath,String path, String originalName) {
        super(fullPath,path, originalName);
    }
}
