package com.auction.usedauction.security;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ErrorCode;
import com.auction.usedauction.exception.error_code.SecurityErrorCode;
import com.auction.usedauction.util.AuthConstants;
import com.auction.usedauction.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.auction.usedauction.exception.error_code.SecurityErrorCode.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    private final RedisUtil redisUtil;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    private int accessTokenValidity;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private int refreshTokenValidity;

    private Key key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + accessTokenValidity * 1000);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .setSubject(authentication.getName())
                .claim(AuthConstants.AUTH_HEADER, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    // 토큰 생성
    public TokenDTO createTokenDTO(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .setSubject(authentication.getName())
                .claim(AuthConstants.AUTH_HEADER, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(now + accessTokenValidity * 1000))
                .compact();

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, "JWT")
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(now + refreshTokenValidity * 1000))
                .compact();

        return new TokenDTO(accessToken, refreshToken);
    }

    // refresh 토큰 저장
    public void saveToken(Authentication authentication, String refreshToken) {
        redisUtil.setData("RefreshToken:" + authentication.getName(), refreshToken, Duration.ofSeconds(refreshTokenValidity));
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaim(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AuthConstants.AUTH_HEADER).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User user = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

    // jwt filter 토큰 검증
    public boolean isValidToken(String token, HttpServletRequest request) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            setException(request, WRONG_TYPE_TOKEN);
            log.info("잘못된 JWT 서명입니다.");
        }catch(ExpiredJwtException e){
            setException(request, EXPIRED_TOKEN);
            log.info("만료된 JWT 입니다.");
        }catch(UnsupportedJwtException e){
            setException(request, UNSUPPORTED_TOKEN);
            log.info("지원하지 않는 JWT 입니다.");
        }catch(IllegalArgumentException e){
            setException(request, WRONG_TOKEN);
            log.info("JWT가 잘못되었습니다.");
        }
        return false;
    }

    // stomp 토큰 검증
    public boolean isValidTokenStomp(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            log.info("잘못된 JWT 서명입니다.");
        }catch(ExpiredJwtException e){
            log.info("만료된 JWT 입니다.");
        }catch(UnsupportedJwtException e){
            log.info("지원하지 않는 JWT 입니다.");
        }catch(IllegalArgumentException e){
            log.info("JWT가 잘못되었습니다.");
        }
        return false;
    }

    // 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AuthConstants.AUTH_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    // 만료된 토큰이어도 정보를 꺼냄
    private Claims getClaim(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().
                getExpiration();

        return (expiration.getTime() - (new Date()).getTime());
    }

    public boolean isLogoutToken(HttpServletRequest request, String token) {
        String logoutToken = redisUtil.getData(token);
        if(StringUtils.hasText(logoutToken)) {
            setException(request, LOGOUT_TOKEN);
            throw new CustomException(LOGOUT_TOKEN);
        }

        return false;
    }

    private void setException(HttpServletRequest request, ErrorCode errorCode) {
        request.setAttribute("exception", errorCode);
    }
}
