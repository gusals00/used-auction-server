package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.NotificationType;
import com.auction.usedauction.repository.dto.NotificationInfoDTO;
import com.auction.usedauction.repository.dto.QNotificationInfoDTO;
import com.auction.usedauction.repository.dto.QTransConfirmNotificationInfoDTO;
import com.auction.usedauction.repository.dto.TransConfirmNotificationInfoDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.auction.usedauction.domain.QNotification.*;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<NotificationInfoDTO> findUnReadNotifications(String loginId) {
        return queryFactory
                .select(new QNotificationInfoDTO(notification.id, notification.title, notification.content, notification.checked, notification.notificationType, notification.relatedUrl))
                .from(notification)
                .where(notification.member.loginId.eq(loginId),
                        notification.checked.eq(false),
                        notification.notificationType.notIn(NotificationType.BUYER_TRANS_CONFIRM, NotificationType.SELLER_TRANS_CONFIRM))
                .orderBy(notification.createdDate.desc())
                .fetch();
    }

    public List<TransConfirmNotificationInfoDTO> findUnReadTransConfirmNotifications(String loginId) {
        return queryFactory
                .select(new QTransConfirmNotificationInfoDTO(notification.id, notification.title, notification.content, notification.checked, notification.notificationType, notification.relatedUrl))
                .from(notification)
                .where(notification.member.loginId.eq(loginId),
                        notification.checked.eq(false),
                        notification.notificationType.in(NotificationType.BUYER_TRANS_CONFIRM, NotificationType.SELLER_TRANS_CONFIRM))
                .orderBy(notification.createdDate.desc())
                .fetch();
    }
}
