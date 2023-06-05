package com.auction.usedauction.service;

import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.Notification;
import com.auction.usedauction.domain.NotificationType;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.NotificationRepository;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auction.usedauction.domain.NotificationType.BUYER_TRANS_CONFIRM;
import static com.auction.usedauction.domain.NotificationType.SELLER_TRANS_CONFIRM;
import static com.auction.usedauction.exception.error_code.NotificationErrorCode.*;
import static com.auction.usedauction.exception.error_code.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public void read(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        notification.changeChecked(true);
    }

    @Transactional
    public void sendTranConfirmNotification(Long productId, String buyerLoginId, String sellerLoginId, String productName, String sellerName, String buyerName) {
        String sellerAndBuyer = " 판매자 : " + sellerName + ", 구매자 : " + buyerName;
        Notification buyTransConfirm = createNotification(BUYER_TRANS_CONFIRM, productId, buyerLoginId, productName, sellerAndBuyer);
        Notification sellTransConfirm = createNotification(SELLER_TRANS_CONFIRM, productId, sellerLoginId, productName, sellerAndBuyer);
        notificationRepository.saveAll(List.of(buyTransConfirm, sellTransConfirm));

        sseEmitterService.sendNotificationData(buyerLoginId, 1L); // 구매자 거래확정 알림 전송
        sseEmitterService.sendNotificationData(sellerLoginId, 1L); // 판매자 거래확정 알림 전송
    }

    @Transactional
    public void sendBidNotification(Long productId, String loginId, String productName, String bidPrice) {
        Notification notification = createNotification(NotificationType.BID, productId, loginId, productName, bidPrice + "원 입찰");
        notificationRepository.save(notification);

        sseEmitterService.sendNotificationData(loginId, 1L);
    }

    private Notification createNotification(NotificationType type, Long productId, String loginId, String title, String content) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return Notification.builder()
                .checked(false)
                .title(title)
                .content(content)
                .member(member)
                .notificationType(type)
                .relatedUrl("productList/productDetail/" + productId)
                .build();
    }
}
