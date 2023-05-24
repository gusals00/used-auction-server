package com.auction.usedauction.repository;


import io.openvidu.java.client.OpenViduRole;
import io.openvidu.java.client.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class StreamingRepository {

    private Map<Long, Session> mapSessions = new ConcurrentHashMap<>(); // <productId : session>

    private Map<Long, Map<String, OpenViduRole>> mapProductIdTokens = new ConcurrentHashMap<>(); // <productId : <token : role>>

    private Map<Long, String> sessionRecordings = new ConcurrentHashMap<>(); // <productId, recordingId>, 녹화중인 방송

    public Session getSession(Long productId) {
        return mapSessions.get(productId);
    }

    public void addSession(Long productId, Session session) {
        mapSessions.put(productId, session);
    }

    public Session removeSession(Long productId) {
        return mapSessions.remove(productId);
    }

    public boolean existsToken(Long productId) {
        return mapProductIdTokens.get(productId) != null;
    }

    public void addToken(Long productId, String token, OpenViduRole role) {
        if (mapProductIdTokens.get(productId) == null) {
            mapProductIdTokens.put(productId, new ConcurrentHashMap<>());
        }
        mapProductIdTokens.get(productId).put(token, role);
    }

    public void removeProductIdTokens(Long productId) {
        mapProductIdTokens.remove(productId);
    }

    public OpenViduRole removeToken(Long productId, String token) {
        return mapProductIdTokens.get(productId).remove(token);
    }

    public OpenViduRole getTokenRole(Long productId, String token) {
        return mapProductIdTokens.get(productId).get(token);
    }

    //라이브 방송중인지
    public boolean isLive(Long productId) {
        return getSession(productId) != null && existsToken(productId);
    }

    public String getPublisherToken(Long productId) {
        if (mapSessions.get(productId) != null) {
            Map<String, OpenViduRole> rolemap = mapProductIdTokens.get(productId);
            List<String> collect = rolemap.keySet().stream()
                    .filter(openViduRole -> rolemap.get(openViduRole).equals(OpenViduRole.PUBLISHER))
                    .toList();

            return collect.isEmpty() ? null : collect.get(0);
        }
        return null;
    }

    public boolean existRecordingId(Long productId) {
        return sessionRecordings.get(productId) != null;
    }

    public String getRecordingId(Long productId) {
        return sessionRecordings.get(productId);
    }

    public String removeRecordingId(Long productId) {
        return sessionRecordings.remove(productId);
    }

    public void addRecordingId(Long productId, String recordingId) {
        sessionRecordings.put(productId, recordingId);
    }

    public List<Long> getStreamingProductIds() {
        return mapSessions.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
