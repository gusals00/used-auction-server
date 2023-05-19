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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.auction.usedauction.exception.error_code.StreamingErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private final StreamingRepository streamingRepository;
    private final OpenVidu openVidu;
    private final ProductRepository productRepository;
    private final FileService fileService;
    @Value("${INIT_FILE_PATH}")
    private String tempFilePath;
    private Map<Long, String> sessionRecordings = new ConcurrentHashMap<>(); // <productId, recordingId>

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

        String publisherToken = streamingRepository.getPublisherToken(productId);

        // 방송중인 판매자가 재입장하는 경우 이미 존재하는 토큰과 세션 반환
        if (publisherToken != null) {
            log.info("방송중인 판매자가 재입장하는 경우");
            return new OpenviduTokenRes(publisherToken, streamingRepository.getSession(productId).getSessionId());
        }

        // Role associated to this user
        OpenViduRole role = OpenViduRole.PUBLISHER;

        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        RecordingProperties recordingProperties = new RecordingProperties.Builder().outputMode(Recording.OutputMode.COMPOSED).hasAudio(true)
                .hasVideo(true).name("test-record").build();

        SessionProperties sessionProperties = new SessionProperties.Builder()
                .recordingMode(RecordingMode.MANUAL)
                .defaultRecordingProperties(recordingProperties)
                .build();

        try {
            // Create a new OpenVidu Session
            Session session = openVidu.createSession(sessionProperties);
            log.info("New session = {} , sessionId = {}", productId, session.getSessionId());

            // Generate a new Connection with the recently created connectionProperties
            String token = session.createConnection(connectionProperties).getToken();
            log.info("New pub token = {}", token);
            // Store the session and the token in our collections
            streamingRepository.addSession(productId, session);
            streamingRepository.addToken(productId, token, role);

//            log.info("start recording");
//            Recording recording = this.openVidu.startRecording(session.getSessionId(), recordingProperties);
//            this.sessionRecordings.put(productId, recording.getId());
//            log.info("start recording complete");

            return new OpenviduTokenRes(token, session.getSessionId());
        } catch (Exception e) {
            log.error("pub error = ", e);
            throw new CustomException(STREAMING_SERVER_ERROR);
        }
    }

    public OpenviduTokenRes joinPublisherTest(Long productId, String loginId) {
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

        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(role).build();

        RecordingProperties recordingProperties = new RecordingProperties.Builder()
                .outputMode(Recording.OutputMode.COMPOSED)
                .resolution("1280x720")
                .frameRate(24)
                .build();

        SessionProperties sessionProperties = new SessionProperties.Builder()
                .recordingMode(RecordingMode.MANUAL)
                .defaultRecordingProperties(recordingProperties)
                .build();

        try {
            // Create a new OpenVidu Session
            Session session = openVidu.createSession(sessionProperties);
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

    public String startRecording(Long productId, String loginId) {
        // 권한 체크
        Product product = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (!loginId.equals(product.getMember().getLoginId())) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        // 방 존재 x 
        Session session = streamingRepository.getSession(productId);
        if (session == null) {
            throw new CustomException(INVALID_SESSION);
        }

        // 이미 녹화중인 경우
        if (sessionRecordings.get(productId) != null) {
            throw new CustomException(ALREADY_RECORDING);
        }

        // 녹화 파일 이름 -> 녹화 시작 날짜(20230508_1211)
        StringBuilder recordNameBuilder = new StringBuilder();
        String recordName = recordNameBuilder
                .append("record")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))).toString();

        RecordingProperties properties = new RecordingProperties.Builder()
                .name(recordName)
                .build();
        try {
            log.info("start recording");
            Recording recording = this.openVidu.startRecording(session.getSessionId(), properties);
            this.sessionRecordings.put(productId, recording.getId());
            log.info("start recording complete");
            return recording.getId();
        } catch (Exception e) {
            log.error("recording start error, productId={}", productId, e);
            throw new CustomException(STREAMING_SERVER_ERROR);
        }
    }

    public void stopRecording(Long productId) throws OpenViduJavaClientException, OpenViduHttpException {
        log.info("trying to stop recording");

        if (sessionRecordings.get(productId) != null) {
            String recordId = sessionRecordings.remove(productId);
            Recording recording = this.openVidu.stopRecording(recordId);
            log.info("stop recording productId = {}] id = {}, sessionId={}, name = {}, url = {}", productId, recording.getId(), recording.getSessionId(), recording.getName(), recording.getUrl());
            log.info("stop recording complete");

            // s3에 영상 저장 및 db에 s3 url 저장
            sendRecordingFileToS3(recording, productId);
        } else {
            log.info("it is not recording status");
        }
    }

    public void sendRecordingFileToS3(Recording recording, Long productId) {
        String urlStr = recording.getUrl();
        String ext = ".mp4";
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            log.info("임시 파일 생성");
            File file = new File(tempFilePath + recording.getName() + ext);
            copyInputStreamToFile(inputStream, file);
            fileService.registerVideoFile(productId, file);
            if (file.delete()) {
                log.info("임시 파일 삭제");
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyInputStreamToFile(InputStream input, File file) throws IOException {
        // append = false
        try (OutputStream output = new FileOutputStream(file, false)) {
            input.transferTo(output);
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
                    stopRecording(productId);
                    this.sessionRecordings.remove(productId);
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

    //몇명이 방송 시청중인지
    public int getLiveCount(Long productId) {
        if (streamingRepository.getSession(productId) == null) { // 생방송중이 아닌 경우
            return 0;
        } else {
            Session session = streamingRepository.getSession(productId);
            try {
                session.fetch();
            } catch (OpenViduJavaClientException e) {
                log.error("liveCount error", e);
            } catch (OpenViduHttpException e) {
                log.error("liveCount error", e);
            }

            return session.getActiveConnections().size() - 1;
        }
    }
}
