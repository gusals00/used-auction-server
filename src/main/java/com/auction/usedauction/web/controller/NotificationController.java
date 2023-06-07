package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.dto.NotificationInfoDTO;
import com.auction.usedauction.repository.query.NotificationQueryRepository;
import com.auction.usedauction.service.NotificationService;
import com.auction.usedauction.service.dto.TransConfirmNotificationInfoRes;
import com.auction.usedauction.service.query.NotificationQueryService;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "alarm controller", description = "알림 관련 api")
public class NotificationController {

    private final NotificationQueryRepository notificationQueryRepository;
    private final NotificationQueryService notificationQueryService;
    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "안읽은 일반 알림 조회")
    @GetMapping
    public ResultRes<NotificationInfoDTO> getUnReadNotifications(@AuthenticationPrincipal User user) {
        return new ResultRes(notificationQueryRepository.findUnReadNotifications(user.getUsername()));
    }

    @Operation(summary = "안읽은 거래확정 알림 조회")
    @GetMapping("/trans-confirm")
    public ResultRes<TransConfirmNotificationInfoRes> getUnReadTransConfirmNotifications(@AuthenticationPrincipal User user) {
        return new ResultRes(notificationQueryService.findUnReadTransConfirmNotifications(user.getUsername()));
    }

    @Operation(summary = "알림 읽음처리")
    @PostMapping("/{notificationId}")
    public ResultRes<MessageRes> readNotification(@PathVariable Long notificationId, @AuthenticationPrincipal User user) {
        notificationService.read(notificationId);

        sseEmitterService.sendNotificationData(user.getUsername(), -1L);

        return new ResultRes(new MessageRes("알림 읽음처리 성공"));
    }
}
