package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.Member;
import com.auction.usedauction.util.AuthConstants;
import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.util.CurrentUser;
import com.auction.usedauction.web.dto.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@RequestBody @Valid LoginReq loginReq) {
        String token = memberService.login(loginReq.getLoginId(), loginReq.getPassword());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);

        return new ResponseEntity<>(new LoginRes(token), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResultRes register(@RequestBody @Valid RegisterReq registerReq, HttpSession session) {
        memberService.register(registerReq, session);

        return new ResultRes(new MessageRes("회원가입 성공"));
    }
}
