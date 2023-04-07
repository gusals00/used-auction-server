package com.auction.usedauction.web.controller;

import com.auction.usedauction.util.AuthConstants;
import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "회원 컨트롤러", description = "회원 관련 api")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ResultRes<LoginRes>> login(@RequestBody @Valid LoginReq loginReq) {
        String token = memberService.login(loginReq.getLoginId(), loginReq.getPassword());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AuthConstants.AUTH_HEADER, AuthConstants.TOKEN_TYPE + " " + token);

        return new ResponseEntity<>(new ResultRes(new LoginRes(token)), httpHeaders, HttpStatus.OK);
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResultRes<MessageRes> register(@RequestBody @Valid RegisterReq registerReq, HttpSession session) {
        memberService.register(registerReq, session);

        return new ResultRes(new MessageRes("회원가입 성공"));
    }


    @Operation(summary = "회원탈퇴")
    @DeleteMapping
    public ResultRes<MessageRes> delete(@AuthenticationPrincipal User user, String password) {
        memberService.delete(user.getUsername(), password);

        return new ResultRes(new MessageRes("회원탈퇴 성공"));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/email/{email}/exists")
    public ResultRes<Boolean> checkEmailDuplicate(@PathVariable String email) {
        return new ResultRes(memberService.checkEmailDuplicate(email));
    }

    @Operation(summary = "아이디 중복 확인")
    @GetMapping("/loginid/{loginid}/exists")
    public ResultRes<Boolean> checkLoginIdDuplicate(@PathVariable String loginid) {
        return new ResultRes(memberService.checkLoginIdDuplicate(loginid));
    }

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("name/{name}/exists")
    public ResultRes<Boolean> checkNameDuplicate(@PathVariable String name) {
        return new ResultRes(memberService.checkNameDuplicate(name));
    }
}
