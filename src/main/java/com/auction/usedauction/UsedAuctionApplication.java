package com.auction.usedauction;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;


@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
public class UsedAuctionApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsedAuctionApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() { // 수정 예정
		return () -> Optional.of(UUID.randomUUID().toString());
	}

	@PostConstruct
	public void setTimeZone() { // LocalDateTime 한국 시간 기준으로
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("LocalDateTime.now()={}",LocalDateTime.now());
	}
}
