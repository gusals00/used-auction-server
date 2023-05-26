package com.auction.usedauction.service.query;

import com.auction.usedauction.repository.dto.ProductInfoDTO;
import com.auction.usedauction.repository.dto.TransConfirmNotificationInfoDTO;
import com.auction.usedauction.repository.query.NotificationQueryRepository;
import com.auction.usedauction.repository.query.ProductQueryRepository;
import com.auction.usedauction.service.dto.TransConfirmNotificationInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationQueryService {

    private final NotificationQueryRepository notificationQueryRepository;
    private final ProductQueryRepository productQueryRepository;

    public List<TransConfirmNotificationInfoRes> findUnReadTransConfirmNotifications(String loginId) {
        List<TransConfirmNotificationInfoDTO> notifications = notificationQueryRepository.findUnReadTransConfirmNotifications(loginId);

        return notifications.stream()
                .map(dtos -> new TransConfirmNotificationInfoRes(dtos, productQueryRepository.findSuccessBidProductInfoById(dtos.getProductId())))
                .collect(Collectors.toList());
    }
}
