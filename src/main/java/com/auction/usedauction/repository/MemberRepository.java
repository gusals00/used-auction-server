package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = "authorities")
    Optional<Member> findOneWithAuthoritiesByLoginId(String loginId);

    @EntityGraph(attributePaths = "authorities")
    Optional<Member> findOneWithAuthoritiesByLoginIdAndStatus(String loginId, MemberStatus memberStatus);
}
