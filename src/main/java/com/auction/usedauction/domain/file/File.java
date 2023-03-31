package com.auction.usedauction.domain.file;

import com.auction.usedauction.domain.BaseTimeEntity;
import com.auction.usedauction.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class File extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    private String path;
    private String fullPath;
    private String originalName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public File(String fullPath,String path, String originalName) {
        this.path = path;
        this.fullPath = fullPath;
        this.originalName = originalName;
    }

    public void changeProduct(Product product) {
        if (product != null) {
            this.product = product;
            product.getFileList().add(this);
        }
    }
}
