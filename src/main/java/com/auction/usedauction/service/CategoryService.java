package com.auction.usedauction.service;

import com.auction.usedauction.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auction.usedauction.domain.QMember.member;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final JPAQueryFactory queryFactory;

    public List<Member> test() {
        return queryFactory.selectFrom(member)
                .fetch();
    }
}
