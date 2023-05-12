package com.auction.usedauction.service;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.StreamingRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.OpenviduTokenRes;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import static com.auction.usedauction.exception.error_code.StreamingErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private final StreamingRepository streamingRepository;
    private final OpenVidu openVidu;
    private final ProductRepository productRepository;

    // 판매자 방송 시작
    public OpenviduTokenRes joinPublisher(Long productId, String loginId) {
        // 판매자 - 세션 없어야 가능, 구매자 - 세션 존재해야 가능
        // 권한 체크
        Product product = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAuction().getStatus() != AuctionStatus.BID) {
            throw new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING);
        }
        if (!loginId.equals(product.getMember().getLoginId())) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        log.info("Getting a token from OpenVidu Server | {productId}= {}", productId);

        // Role associated to this user
        OpenViduRole role = OpenViduRole.PUBLISHER;

        // Build connectionProperties object with the serverData and the role
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        try {
            // Create a new OpenVidu Session
            Session session = openVidu.createSession();
            log.info("New session = {} , sessionId = {}", productId, session.getSessionId());

            // Generate a new Connection with the recently created connectionProperties
            String token = session.createConnection(connectionProperties).getToken();
            log.info("New pub token = {}", token);

            // Store the session and the token in our collections
            streamingRepository.addSession(productId, session);
            streamingRepository.addToken(productId, token, role);


            return new OpenviduTokenRes(token, session.getSessionId());
        } catch (Exception e) {
            log.error("pub error = ", e);
            throw new CustomException(STREAMING_SERVER_ERROR);
        }
    }

    // 구매자 방송 참여
    public OpenviduTokenRes joinSubscriber(Long productId) {
        // Role associated to this user
        OpenViduRole role = OpenViduRole.SUBSCRIBER;

        // Build connectionProperties object with the serverData and the role
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        if (streamingRepository.getSession(productId) != null) {

            // Session already exists
            log.info("Existing session = {} , sessionId = {}", productId, streamingRepository.getSession(productId).getSessionId());
            try {
                // Generate a new Connection with the recently created connectionProperties
                String token = streamingRepository.getSession(productId).createConnection(connectionProperties).getToken();
                log.info("New sub token = {}", token);

                // Update our collection storing the new token
                streamingRepository.addToken(productId, token, role);
                return new OpenviduTokenRes(token, streamingRepository.getSession(productId).getSessionId());

            } catch (OpenViduJavaClientException e1) {
                log.error("sub error = ", e1);
                throw new CustomException(STREAMING_SERVER_ERROR);
            } catch (OpenViduHttpException e2) {
                if (404 == e2.getStatus()) {
                    // Invalid sessionId (user left unexpectedly). Session object is not valid
                    // anymore. Clean collections and continue as new session
                    streamingRepository.removeSession(productId);
                    streamingRepository.removeProductIdTokens(productId);
                }
            }
        }

        throw new CustomException(INVALID_SESSION);
    }

    // 방송 종료(Publisher)
    public Session closeSession(Long productId, String token, String loginId) {

        log.info("Removing user | {productId, token} = {}, {}", productId, token);

        // 권한 체크
        Product product = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (!loginId.equals(product.getMember().getLoginId())) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }

        // If the session exists
        if (streamingRepository.getSession(productId) != null && streamingRepository.existsToken(productId)) {

            // If the token is publisher
            if (streamingRepository.getTokenRole(productId, token) == OpenViduRole.PUBLISHER) {
                // User left the session
                Session removedSession = streamingRepository.removeSession(productId);
                streamingRepository.removeProductIdTokens(productId);
                try {
                    removedSession.close();
                } catch (Exception e) {
                    log.error("pub close error = ", e);
                    throw new CustomException(STREAMING_SERVER_ERROR);
                }

                return removedSession;
            } else {
                // The TOKEN wasn't valid
                log.info("Problems in the app server: the TOKEN wasn't valid");
                throw new CustomException(INVALID_STREAMING_PUBLISHER);
            }

        } else {
            // The SESSION does not exist
            log.info("Problems in the app server: the SESSION does not exist");
            throw new CustomException(INVALID_SESSION);
        }
    }

    // 방 퇴장(Subscriber)
    public String exitSession(Long productId, String token) {
        log.info("Removing user | {productId, token} = {}, {}", productId, token);

        // If the session exists
        if (streamingRepository.getSession(productId) != null && streamingRepository.existsToken(productId)) {

            //  If the token is subscriber
            if (streamingRepository.getTokenRole(productId, token) == OpenViduRole.SUBSCRIBER) {
                streamingRepository.removeToken(productId, token);
                return token;
            } else {
                // The TOKEN wasn't valid
                log.info("Problems in the app server: the TOKEN wasn't valid");
                throw new CustomException(INVALID_STREAMING_SUBSCRIBER);
            }

        } else {
            // The SESSION does not exist
            log.info("Problems in the app server: the SESSION does not exist");
            throw new CustomException(STREAMING_END);
        }
    }
}
