package com.auction.usedauction.security;

import com.auction.usedauction.exception.ErrorRes;
import com.auction.usedauction.exception.error_code.ErrorCode;
import com.auction.usedauction.exception.error_code.SecurityErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.auction.usedauction.exception.error_code.SecurityErrorCode.*;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

// 권한 체크 후 인가 실패 시
 @Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(SC_FORBIDDEN);

        sendError(response, ACCESS_DENIED);
    }

    private void sendError(HttpServletResponse response, ErrorCode exception) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), new ErrorRes(exception));
    }
}
