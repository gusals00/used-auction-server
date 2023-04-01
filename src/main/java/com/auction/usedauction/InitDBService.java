package com.auction.usedauction;

import com.auction.usedauction.domain.Authority;
import com.auction.usedauction.domain.Category;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InitDBService {

    private final EntityManager em;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initDb() {

        //Category 추가
        insertCategory();

        // member +   Authority Role_user 추가
        insertMember();
    }

    private Long insertMember() {
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        Member member = Member.builder()
                .name("name")
                .birth("990828")
                .email("a@naver.com")
                .loginId("hyeonmin")
                .password(passwordEncoder.encode("password"))
                .phoneNumber("010-1233-1233")
                .authorities(Collections.singleton(authority))
                .build();
        em.persist(authority);
        em.persist(member);

        return member.getId();
    }

    @Transactional
    public void insertCategory() {
        List<Category> categoryList = new ArrayList<>(Arrays.asList(
                createCategory("디지털기기"), createCategory("생활가전"), createCategory("가구/인테리어"),
                createCategory("생활/주방"), createCategory("유아동"), createCategory("유아도서"),
                createCategory("여성의류"), createCategory("여성잡화"), createCategory("도서"),
                createCategory("가공식품"), createCategory("반려동물용품"), createCategory("식품"),
                createCategory("기타"), createCategory("남성패션/잡화"), createCategory("뷰티/미용"),
                createCategory("티켓/교환권"), createCategory("스포츠/레저"), createCategory("취미/게임/음반")
        ));
        categoryRepository.saveAll(categoryList);
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

}
