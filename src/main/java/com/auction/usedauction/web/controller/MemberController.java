package com.auction.usedauction.web.controller;

import com.auction.usedauction.loadTest.InitLoadTest;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "member controller", description = "회원 관련 api")
public class MemberController {

    private final MemberService memberService;
    private final InitLoadTest loadTest;
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResultRes<TokenDTO> login(@RequestBody @Valid LoginReq loginReq) {
        TokenDTO token = memberService.login(loginReq.getLoginId(), loginReq.getPassword());
        return new ResultRes(token);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResultRes<TokenDTO> reissue(@RequestBody @Valid TokenDTO tokenDTO) {
        TokenDTO token = memberService.reissue(tokenDTO);
        return new ResultRes(token);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResultRes<MessageRes> logout(@AuthenticationPrincipal User user, @RequestBody LogoutReq logoutReq) {
        memberService.logout(user, logoutReq);

        return new ResultRes(new MessageRes("로그아웃 성공"));
    }

    @Operation(summary = "로그인 체크")
    @GetMapping("/is-login")
    public ResultRes<LoginCheckRes> loginCheck(@AuthenticationPrincipal User user) {
        return new ResultRes(memberService.getLoginCheck(user));
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
