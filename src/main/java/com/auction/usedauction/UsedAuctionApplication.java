package com.auction.usedauction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@EnableJpaAuditing
public class UsedAuctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsedAuctionApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() { //세션 정보에서 가져오는걸로 수정하기
		return () -> Optional.of(UUID.randomUUID().toString());
	}
}
