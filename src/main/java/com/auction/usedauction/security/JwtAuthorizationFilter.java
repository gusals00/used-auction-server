package com.auction.usedauction.security;

import com.auction.usedauction.util.AuthConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if(request.getMethod().equals("OPTIONS")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader(AuthConstants.AUTH_HEADER);
        String jwt = tokenProvider.resolveToken(bearerToken);

        if(StringUtils.hasText(jwt) && tokenProvider.isValidToken(jwt, request)) { // 유효성 검증
            if(!tokenProvider.isLogoutToken(request, jwt)) { // 로그아웃 여부 확인
                Authentication authentication = tokenProvider.getAuthentication(jwt);

                // 스프링 시큐리티 유저를 시큐리티 컨텍스트에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("SecurityContext에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), request.getRequestURI());
            }
        } else {
            log.info("유효한 JWT 토큰이 없습니다, uri: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
