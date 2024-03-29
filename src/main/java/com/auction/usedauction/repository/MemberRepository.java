package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = "authorities")
    Optional<Member> findOneWithAuthoritiesByLoginId(String loginId);

    @EntityGraph(attributePaths = "authorities")
    Optional<Member> findOneWithAuthoritiesByLoginIdAndStatus(String loginId, MemberStatus memberStatus);
    Optional<Member> findOneByLoginIdAndStatus(String loginId, MemberStatus memberStatus);
    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByName(String name);

    @Query(value = "select m.id from Member m where m.loginId = :loginId")
    Long findIdByLoginId(@Param("loginId") String loginId);
}
