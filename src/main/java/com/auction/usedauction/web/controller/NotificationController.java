package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.dto.NotificationInfoDTO;
import com.auction.usedauction.repository.query.NotificationQueryRepository;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "알림 컨트롤러", description = "알림 관련 api")
public class NotificationController {

    private final NotificationQueryRepository notificationQueryRepository;

    @Operation(summary = "안읽은 일반 알림 조회")
    @GetMapping
    public ResultRes<NotificationInfoDTO> getUnReadNotifications(@AuthenticationPrincipal User user) {
        return new ResultRes(notificationQueryRepository.findUnReadNotifications(user.getUsername()));
    }

    @Operation(summary = "안읽은 거래확정 알림 조회")
    @GetMapping("/trans-confirm")
    public ResultRes getUnReadTransConfirmNotifications(@AuthenticationPrincipal User user) {
        return new ResultRes(notificationQueryRepository.findUnReadTransConfirmNotifications(user.getUsername()));
    }
}
