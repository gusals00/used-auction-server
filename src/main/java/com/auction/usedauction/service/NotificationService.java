package com.auction.usedauction.service;

import com.auction.usedauction.domain.Notification;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auction.usedauction.exception.error_code.NotificationErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void read(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

        notification.changeChecked(true);
    }
}
