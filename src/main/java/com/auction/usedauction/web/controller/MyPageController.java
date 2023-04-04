package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.service.dto.MemberDetailInfoRes;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import com.auction.usedauction.web.dto.UserUpdateReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Tag(name = "마이페이지 컨트롤러", description = "마이페이지 관련 api")
public class MyPageController {

     private final MemberService memberService;

    @Operation(summary = "회원정보 조회")
     @GetMapping
    public ResultRes<MemberDetailInfoRes> getInfo(@AuthenticationPrincipal User user) {
         return new ResultRes(memberService.getInfo(user.getUsername()));
     }

    @Operation(summary = "회원정보 수정")
     @PatchMapping
    public ResultRes<MessageRes> updateInfo(@AuthenticationPrincipal User user, @RequestBody @Valid UserUpdateReq userUpdateReq) {
         memberService.updateInfo(user.getUsername(), userUpdateReq);

         return new ResultRes(new MessageRes("회원정보 수정 성공"));
     }

}
