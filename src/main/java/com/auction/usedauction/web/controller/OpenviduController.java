package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.web.dto.*;
import io.openvidu.java.client.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.auction.usedauction.exception.error_code.StreamingErrorCode.*;

@RestController
@Profile(value = {"local", "production"})
@RequestMapping("/api/sessions")
@Tag(name = "스트리밍 컨트롤러", description = "스트리밍 관련 api")
@Slf4j
@RequiredArgsConstructor
public class OpenviduController {

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openVidu;


    // TODO repository, service 만들어서 분리하기
    private Map<Long, Session> mapSessions = new ConcurrentHashMap<>(); // <productId, session>

    private Map<Long, Map<String, OpenViduRole>> mapProductIdTokens = new ConcurrentHashMap<>();

    private final ProductRepository productRepository;

    @PostConstruct
    public void init() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /**
     * @param params The Session properties
     * @return The Session ID
     */
    @PostMapping
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
        // 판매자 - 세션 없어야 가능, 구매자 - 세션 존재해야 가능
        Long productId = openviduTokenReq.getProductId();
        // 권한 체크
        Product product = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAuction().getStatus() != AuctionStatus.BID){
            throw new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING);
        }
        if (!user.getUsername().equals(product.getMember().getLoginId())){
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        log.info("Getting a token from OpenVidu Server | {productId}= {}", openviduTokenReq.getProductId());

        // Role associated to this user
        OpenViduRole role = OpenViduRole.PUBLISHER;

        // Build connectionProperties object with the serverData and the role
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        try {
            // Create a new OpenVidu Session
            Session session = this.openVidu.createSession();
            log.info("New session = {} , sessionId = {}", productId, session.getSessionId());

            // Generate a new Connection with the recently created connectionProperties
            String token = session.createConnection(connectionProperties).getToken();
            log.info("New pub token = {}", token);

            // Store the session and the token in our collections
            this.mapSessions.put(productId, session);
            this.mapProductIdTokens.put(productId, new ConcurrentHashMap<>());
            this.mapProductIdTokens.get(productId).put(token, role);

            return new ResultRes(new OpenviduTokenRes(token, session.getSessionId()));
        } catch (Exception e) {
            log.error("pub error = ", e);
            throw new CustomException(STREAMING_SERVER_ERROR);
        }
    }

    @PostMapping("/get-token-sub")
    @Operation(summary = "Subscriber 토큰 생성")
    public ResultRes<OpenviduTokenRes> getTokenSubscriber(@RequestBody OpenviduTokenReq openviduTokenReq) {

        log.info("Getting a token from OpenVidu Server | {productId}= {}", openviduTokenReq.getProductId());

        // Role associated to this user
        OpenViduRole role = OpenViduRole.SUBSCRIBER;

        Long productId = openviduTokenReq.getProductId();

        // Build connectionProperties object with the serverData and the role
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        if (this.mapSessions.get(productId) != null) {
            // Session already exists
            log.info("Existing session = {} , sessionId = {}", productId, mapSessions.get(productId).getSessionId());
            try {
                // Generate a new Connection with the recently created connectionProperties
                String token = this.mapSessions.get(productId).createConnection(connectionProperties).getToken();
                log.info("New sub token = {}", token);

                // Update our collection storing the new token
                this.mapProductIdTokens.get(productId).put(token, role);

                return new ResultRes(new OpenviduTokenRes(token, mapSessions.get(productId).getSessionId()));

            } catch (OpenViduJavaClientException e1) {
                log.error("sub error = ", e1);
                throw new CustomException(STREAMING_SERVER_ERROR);
            } catch (OpenViduHttpException e2) {
                if (404 == e2.getStatus()) {
                    // Invalid sessionId (user left unexpectedly). Session object is not valid
                    // anymore. Clean collections and continue as new session
                    this.mapSessions.remove(productId);
                    this.mapProductIdTokens.remove(productId);
                }
            }
        }

        throw new CustomException(INVALID_SESSION);
    }

    @PostMapping("/remove-user-pub")
    @Operation(summary = "Publisher 방송 종료")
    public ResultRes removeUserPublisher(@RequestBody OpenviduRemoveUserReq openviduRemoveUserReq, @AuthenticationPrincipal User user) {

        Long productId = openviduRemoveUserReq.getProductId();
        String token = openviduRemoveUserReq.getToken();
        log.info("Removing user | {productId, token} = {}, {}", productId, token);

        // 권한 체크
        Product product = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (!user.getUsername().equals(product.getMember().getLoginId())){
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        // If the session exists
        if (this.mapSessions.get(productId) != null && this.mapProductIdTokens.get(productId) != null) {

            // If the token exists
            if (this.mapProductIdTokens.get(productId).remove(token) != null) {
                // User left the session

                Session removedSession = this.mapSessions.remove(productId);
                mapProductIdTokens.remove(productId);
                try {
                    removedSession.close();
                } catch (Exception e) {
                    log.error("pub close error = ", e);
                    throw new CustomException(STREAMING_SERVER_ERROR);
                }

                return new ResultRes(new MessageRes("방송이 종료되었습니다."));
            } else {
                // The TOKEN wasn't valid
                log.info("Problems in the app server: the TOKEN wasn't valid");
                throw new CustomException(INVALID_STREAMING_TOKEN);
            }

        } else {
            // The SESSION does not exist
            log.info("Problems in the app server: the SESSION does not exist");
            throw new CustomException(INVALID_SESSION);
        }
    }

    @PostMapping("/remove-user-sub")
    @Operation(summary = "Subscriber 방 퇴장")
    public ResultRes removeUserSubscriber(@RequestBody OpenviduRemoveUserReq openviduRemoveUserReq) {
        Long productId = openviduRemoveUserReq.getProductId();
        String token = openviduRemoveUserReq.getToken();
        log.info("Removing user | {productId, token} = {}, {}", productId, token);

        // If the session exists
        if (this.mapSessions.get(productId) != null && this.mapProductIdTokens.get(productId) != null) {

            // If the token exists
            if (this.mapProductIdTokens.get(productId).remove(token) != null) {

                return new ResultRes(new MessageRes("방 퇴장 성공"));
            } else {
                // The TOKEN wasn't valid
                log.info("Problems in the app server: the TOKEN wasn't valid");
                throw new CustomException(INVALID_STREAMING_TOKEN);
            }

        } else {
            // The SESSION does not exist
            log.info("Problems in the app server: the SESSION does not exist");
            throw new CustomException(STREAMING_END);
        }
    }
}