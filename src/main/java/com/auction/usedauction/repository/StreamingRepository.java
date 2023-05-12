package com.auction.usedauction.repository;


import io.openvidu.java.client.OpenViduRole;
import io.openvidu.java.client.Session;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StreamingRepository {

    private Map<Long, Session> mapSessions = new ConcurrentHashMap<>(); // <productId : session>

    private Map<Long, Map<String, OpenViduRole>> mapProductIdTokens = new ConcurrentHashMap<>(); // <productId : <token : role>>

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
        if(mapProductIdTokens.get(productId) == null) {
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

    public boolean isLive(Long productId) {
        return getSession(productId)!=null && existsToken(productId);
    }
}
