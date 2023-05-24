package com.auction.usedauction.service;

import com.auction.usedauction.domain.Authority;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.SecurityErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.security.TokenProvider;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.service.dto.MemberDetailInfoRes;
import com.auction.usedauction.util.RedisUtil;
import com.auction.usedauction.web.dto.LoginCheckRes;
import com.auction.usedauction.web.dto.LogoutReq;
import com.auction.usedauction.web.dto.RegisterReq;
import com.auction.usedauction.web.dto.UserUpdateReq;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.auction.usedauction.util.AuthConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisUtil redisUtil;
    private final StreamingService streamingService;


    // id와 password로 사용자를 인증해서 토큰을 반환함.
    public TokenDTO login(String loginId, String password) {
        // id와 password를 통해 UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);

        // UsernamePasswordAuthenticationToken을 통해 Authentication 객체 생성
        // 여기서 UserDetailsServiceImpl에 있는 loadUserByUsername 호출
        Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 시큐리티 컨텍스트에 저장
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        // 인증 정보를 통해 토큰 생성
        TokenDTO tokenDTO = tokenProvider.createTokenDTO(authenticate);

        // refresh 토큰 redis 저장
        tokenProvider.saveToken(authenticate, tokenDTO.getRefreshToken());

        return tokenDTO;
    }

    public TokenDTO reissue(TokenDTO tokenDTO) {
        // access 토큰에서 loginId 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenDTO.getAccessToken());

        // redis에서 refresh 토큰 가져오기
        String refreshToken = redisUtil.getData("RefreshToken:" + authentication.getName());

        // refresh 토큰 검증
        if(!refreshToken.equals(tokenDTO.getRefreshToken())) {
            throw new CustomException(SecurityErrorCode.WRONG_TOKEN);
        }

        // 새 토큰 생성
        TokenDTO tokenRes = tokenProvider.createTokenDTO(authentication);

        // redis에 새 토큰 저장
        tokenProvider.saveToken(authentication, tokenRes.getRefreshToken());

        return tokenRes;
    }

    public void logout(User user, LogoutReq logoutReq) {
        // refresh 토큰 확인
        if(redisUtil.getData("RefreshToken:" + user.getUsername()) != null) {
            // refresh 토큰 삭제
            redisUtil.deleteData("RefreshToken:" + user.getUsername());
        }

        // access 토큰 유효시간
        Long expiration = tokenProvider.getExpiration(logoutReq.getAccessToken());

        log.info("expire = {} ", expiration);

        // blacklist 등록
        redisUtil.setData(logoutReq.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);

        //판매자가 방송중인 방송 종료
        streamingService.closeSellerAllSessions(user.getUsername());
    }

    @Transactional
    public Long register(RegisterReq registerReq, HttpSession session) {

        // 이메일 인증 확인
        if(!emailService.isAuthenticated(session, registerReq.getEmail(), registerReq.getCode())) {
            throw new CustomException(UserErrorCode.EMAIL_AUTH_FAIL);
        }

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

    @Transactional
    public Long delete(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if(!passwordCheck(password, member.getPassword())) {
            throw new CustomException(UserErrorCode.WRONG_PASSWORD);
        }

        member.changeStatus(MemberStatus.DELETED);

        return member.getId();
    }

    public MemberDetailInfoRes getInfo(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return new MemberDetailInfoRes(member);
    }

    public LoginCheckRes getLoginCheck(User user) {
        if(user == null) {
            return new LoginCheckRes(false, "", "");
        } else {
            String loginId = user.getUsername();
            String name =  memberRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND)).getName();
            return new LoginCheckRes(true, loginId, name);
        }
    }

    @Transactional
    public Long updateInfo(String loginId, UserUpdateReq userUpdateReq) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        member.changeMember(userUpdateReq.getName(), userUpdateReq.getBirth(), userUpdateReq.getPhoneNumber());

        return member.getId();
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean checkLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public boolean checkNameDuplicate(String name) {
        return memberRepository.existsByName(name);
    }

    private boolean passwordCheck(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

}
