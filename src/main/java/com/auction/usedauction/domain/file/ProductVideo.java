package com.auction.usedauction.domain.file;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVideo extends File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_video_id")
    private Long id;

    @Builder
    public ProductVideo(String fullPath,String path, String originalName) {
        super(fullPath,path, originalName);
    }
}
