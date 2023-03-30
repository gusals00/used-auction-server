package com.auction.usedauction;

import com.auction.usedauction.domain.Authority;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InitDBService {

    private final EntityManager em;

    public void initDb() {
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        em.persist(authority);
    }

}
