package com.auction.usedauction.repository.dto;

import com.auction.usedauction.domain.NotificationType;
import com.auction.usedauction.exception.CustomException;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransConfirmNotificationInfoDTO {

    private Long notificationId;

    private Long productId;

    private String title;

    private String content;

    private boolean checked;

    private String type;

    private String relatedUrl;

    @QueryProjection
    public TransConfirmNotificationInfoDTO(Long notificationId, String title, String content, boolean checked, NotificationType type, String relatedUrl) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.checked = checked;
        this.type = type.getDescription();
        this.relatedUrl = relatedUrl;
        int lastIndex = relatedUrl.lastIndexOf('/');
        if (lastIndex != -1) {
            productId = Long.valueOf(relatedUrl.substring(lastIndex + 1));
        }
    }
}
