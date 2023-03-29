package com.auction.usedauction.service;

import com.auction.usedauction.domain.Authority;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.security.TokenProvider;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.web.dto.RegisterReq;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.auction.usedauction.util.AuthConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // id와 password로 사용자를 인증해서 엑세스토큰을 반환함.
    public String login(String loginId, String password) {
        // id와 password를 통해 UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);

        // UsernamePasswordAuthenticationToken을 통해 Authentication 객체 생성
        // 여기서 UserDetailsServiceImpl에 있는 loadUserByUsername 호출
        Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 시큐리티 컨텍스트에 저장
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        // 인증 정보를 통해 엑세스 토큰 생성
        String token = tokenProvider.createToken(authenticate);
        return token;
    }

    @Transactional
    public Long register(RegisterReq registerReq, HttpSession session) {
        // 중복체크 추가하기

        // 이메일 인증 확인
//        if(!emailService.isAuthenticated(session, registerReq.getEmail(), registerReq.getCode())) {
//            // 예외처리하기
//        }

        session.removeAttribute(EMAIL_AUTH);
        
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        Member member = Member.builder()
                .name(registerReq.getName())
                .birth(registerReq.getBirth())
                .email(registerReq.getEmail())
                .loginId(registerReq.getLoginId())
                .password(passwordEncoder.encode(registerReq.getPassword()))
                .phoneNumber(registerReq.getPhoneNumber())
                .authorities(Collections.singleton(authority))
                .build();

        return memberRepository.save(member).getId();
    }
}
