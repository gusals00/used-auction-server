package com.auction.usedauction.domain;

import com.auction.usedauction.domain.file.File;
import com.auction.usedauction.domain.file.ProductImage;
import com.auction.usedauction.domain.file.ProductImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String info;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    private int viewCount;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> fileList = new ArrayList<>();

    @Builder
    public Product(String name, String info, Auction auction, Category category,
                   Member member, List<ProductImage> ordinalImageList, ProductImage sigImage) {

        if (member != null) {
            member.getProducts().add(this);
            this.member = member;
        }
        if (sigImage != null) {
            sigImage.changeProduct(this);
        }
        if (ordinalImageList != null) {
            ordinalImageList.forEach(ordinalImage -> ordinalImage.changeProduct(this));
        }

        this.name = name;
        this.info = info;
        this.viewCount = 0;
        this.category = category;
        this.auction = auction;
    }


    public ProductImage getSigImage() {
        for (File file :fileList) {
            if (file instanceof ProductImage imageFile) {
                if (imageFile.getType() == ProductImageType.SIGNATURE) {
                    return imageFile;
                }
            }
        }
        return null;
    }

    public List<ProductImage> getOrdinalImageList() {
        List<ProductImage> ordinalImages = new ArrayList<>();

        for (File file :fileList) {
            if (file instanceof ProductImage imageFile) {
                if (imageFile.getType() == ProductImageType.ORDINAL) {
                   ordinalImages.add(imageFile);
                }
            }
        }
        return ordinalImages;
    }

    public int increaseViewCount() {
        viewCount+=1;
        return viewCount;
    }

    public void changeProductStatus(ProductStatus status) {
        this.productStatus = status;
    }

    public void changeProduct(String name,String info, Category category, Auction auction) {
        this.name =name;
        this.info=info;
        this.category=category;
        this.auction = auction;
    }

    @PrePersist
    private void initProductStatus() {
        // 상품 생성시 바로 존재상태로
        this.productStatus = ProductStatus.EXIST;
    }

}
