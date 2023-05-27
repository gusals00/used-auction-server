package com.auction.usedauction.service.dto;

import com.auction.usedauction.repository.dto.ProductInfoDTO;
import com.auction.usedauction.repository.dto.TransConfirmNotificationInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransConfirmNotificationInfoRes {

    private Long notificationId;

    private Long productId;

    private String title;

    private String content;

    private boolean checked;

    private String type;

    private String relatedUrl;


    private String sigImgSrc;

    private Integer endPrice;

    public TransConfirmNotificationInfoRes(TransConfirmNotificationInfoDTO notificationInfoDTO, ProductInfoDTO productInfoDTO) {
        this.notificationId = notificationInfoDTO.getNotificationId();
        this.productId = notificationInfoDTO.getProductId();
        this.title = notificationInfoDTO.getTitle();
        this.content = notificationInfoDTO.getContent();
        this.checked = notificationInfoDTO.isChecked();
        this.type = notificationInfoDTO.getType();
        this.relatedUrl = notificationInfoDTO.getRelatedUrl();
        this.sigImgSrc = productInfoDTO.getSigImgSrc();
        this.endPrice = productInfoDTO.getEndPrice();
    }
}
