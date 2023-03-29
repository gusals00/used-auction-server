package com.auction.usedauction.security;

import com.auction.usedauction.domain.Member;
import com.auction.usedauction.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findOneWithAuthoritiesByLoginId(loginId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        return new MemberAdapter(member);
    }
}
