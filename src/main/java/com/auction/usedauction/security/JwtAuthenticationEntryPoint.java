package com.auction.usedauction.security;

import com.auction.usedauction.exception.ErrorRes;
import com.auction.usedauction.exception.error_code.ErrorCode;
import com.auction.usedauction.exception.error_code.SecurityErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.auction.usedauction.exception.error_code.SecurityErrorCode.*;
import static jakarta.servlet.http.HttpServletResponse.*;


// 인증 실패 시
 @Component
 @Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode exception = (ErrorCode) request.getAttribute("exception");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(SC_UNAUTHORIZED);

        if(exception == null) {
            sendError(response, UNAUTHORIZED);
        } else {
            sendError(response, exception);
        }
    }

    private void sendError(HttpServletResponse response, ErrorCode exception) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), new ErrorRes(exception));
    }
}
