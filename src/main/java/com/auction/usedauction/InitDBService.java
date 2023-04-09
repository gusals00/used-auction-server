package com.auction.usedauction;

import com.auction.usedauction.domain.Authority;
import com.auction.usedauction.domain.Category;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.file.FileRepository;
import com.auction.usedauction.util.S3FileUploader;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InitDBService {

    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3FileUploader fileUploader;
    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final EntityManager em;

    @Transactional
    public void initDb() {

        //Category 추가
        insertCategory();

        // member + Authority ROLE_USER 추가
        insertMember();
    }

    private Long insertMember() {
        Authority authority = createAuthority("ROLE_USER");
        em.persist(authority);

        Member member1 = createMember("현민", "990828", "a111@naver.com", "hyeonmin", "password", "010-1233-1233", authority);
        Member member2 = createMember("대현", "990128", "ab@naver.com", "11", "11", "010-2222-3333", authority);
        Member member3 = createMember("병관", "990428", "addd@naver.com", "20180004", "1128", "010-4444-8888", authority);

        memberRepository.saveAll(Arrays.asList(member1,member2,member3));

        return member1.getId();
    }

    private Member createMember(String name, String birth, String email, String loginId, String password, String phoneNumber, Authority authorities) {
        return Member.builder()
                .name(name)
                .birth(birth)
                .email(email)
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .authorities(Collections.singleton(authorities))
                .build();
    }

    private Authority createAuthority(String name) {
        return Authority.builder()
                .authorityName(name)
                .build();
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

    @PreDestroy
    public void deleteS3File() {
        // S3에 저장된 파일 삭제
        fileRepository.findAll()
                .forEach(file -> fileUploader.deleteFile(file.getPath()));
    }

}
