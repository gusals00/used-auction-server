package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.EmailService;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
@Tag(name = "email controller", description = "이메일 관련 api")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증 이메일 발송")
    @PostMapping("/{email}")
    public ResultRes<String> sendJoinMail(@PathVariable String email, HttpSession session) throws MessagingException, UnsupportedEncodingException {
        return new ResultRes(emailService.sendMail(email, session));
    }
}
