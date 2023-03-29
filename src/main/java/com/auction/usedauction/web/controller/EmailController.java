package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.EmailService;
import com.auction.usedauction.web.dto.ResultRes;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/{email}")
    public ResultRes sendJoinMail(@PathVariable String email, HttpSession session) throws MessagingException, UnsupportedEncodingException {
        return new ResultRes(emailService.sendMail(email, session));
    }
}
