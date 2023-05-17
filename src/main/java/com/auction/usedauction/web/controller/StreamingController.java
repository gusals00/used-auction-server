package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.StreamingRepository;
import com.auction.usedauction.service.StreamingService;
import com.auction.usedauction.service.dto.OpenviduTokenRes;
import com.auction.usedauction.web.dto.*;
import io.openvidu.java.client.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@Profile(value = {"local", "production"})
@RequestMapping("/api/sessions")
@Tag(name = "스트리밍 컨트롤러", description = "스트리밍 관련 api")
@Slf4j
@RequiredArgsConstructor
public class StreamingController {

    private final OpenVidu openVidu;
    private final StreamingService streamingService;
    private final StreamingRepository streamingRepository;

    /**
     * @param params The Session properties
     * @return The Session ID
     */
    @PostMapping
    @Operation(summary = "openvidu tutorial 세션 생성")
    public ResponseEntity<String> initializeSession(@RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openVidu.createSession(properties);
        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

    /**
     * @param sessionId The Session in which to create the Connection
     * @param params    The Connection properties
     * @return The Token associated to the Connection
     */
    @PostMapping("/{sessionId}/connections")
    @Operation(summary = "openvidu tutorial 토큰 생성")
    public ResponseEntity<String> createConnection(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

    @PostMapping("/get-token-pub")
    @Operation(summary = "Publisher 토큰 생성")
    public ResultRes<OpenviduTokenRes> getTokenPublisher(@RequestBody OpenviduTokenReq openviduTokenReq, @AuthenticationPrincipal User user) {

        return new ResultRes<>(streamingService.joinPublisher(openviduTokenReq.getProductId(), user.getUsername()));
    }

    @PostMapping("/get-token-sub")
    @Operation(summary = "Subscriber 토큰 생성")
    public ResultRes<OpenviduTokenRes> getTokenSubscriber(@RequestBody OpenviduTokenReq openviduTokenReq) {

        return new ResultRes<>(streamingService.joinSubscriber(openviduTokenReq.getProductId()));
    }

    @PostMapping("/remove-user-pub")
    @Operation(summary = "Publisher 방송 종료")
    public ResultRes<MessageRes> removeUserPublisher(@RequestBody OpenviduRemoveUserReq openviduRemoveUserReq, @AuthenticationPrincipal User user) {

        streamingService.closeSession(openviduRemoveUserReq.getProductId(), openviduRemoveUserReq.getToken(), user.getUsername());
        return new ResultRes<>(new MessageRes("방송이 종료되었습니다."));
    }

    @PostMapping("/remove-user-sub")
    @Operation(summary = "Subscriber 방 퇴장")
    public ResultRes<MessageRes> removeUserSubscriber(@RequestBody OpenviduRemoveUserReq openviduRemoveUserReq) {

        streamingService.exitSession(openviduRemoveUserReq.getProductId(), openviduRemoveUserReq.getToken());
        return new ResultRes<>(new MessageRes("방 퇴장 성공"));
    }

    @GetMapping("/is-live/{productId}")
    @Operation(summary = "생방송중인지 여부 확인")
    public ResultRes<IsLiveRes> isLive(@PathVariable Long productId) {
        return new ResultRes<>(new IsLiveRes(streamingRepository.isLive(productId)));
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "시청자 수 조회")
    public ResultRes<LiveCountRes> liveCount(@PathVariable Long productId) {
        return new ResultRes<>(new LiveCountRes(streamingService.getLiveCount(productId)));
    }
}