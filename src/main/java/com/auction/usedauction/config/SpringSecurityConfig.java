package com.auction.usedauction.config;

import com.auction.usedauction.security.JwtAuthorizationFilter;
import com.auction.usedauction.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final TokenProvider tokenProvider;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // static 리소스들을 스프링 시큐리티에서 제외
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .cors(Customizer.withDefaults())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // session 기반 인증방식 사용하지 않기 때문에 STATELESS로 설정

                .and()
                .authorizeHttpRequests()
                .requestMatchers("/chat/ws/**").permitAll()
                .requestMatchers("/pub/**").permitAll()
                .requestMatchers("/api/member/signup").permitAll()
                .requestMatchers("/api/member/login").permitAll()
                .requestMatchers("/api/member/is-login").permitAll()
                .requestMatchers("/api/email/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/products").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/products/*").permitAll()
                .requestMatchers("/api/member/email/**").permitAll()
                .requestMatchers("/api/member/loginid/**").permitAll()
                .requestMatchers("/api/member/name/**").permitAll()

                //question
                .requestMatchers("/api/questions/*").permitAll()

                //swagger
                .requestMatchers("/swagger-ui/*").permitAll()
                .requestMatchers("/v3/api-docs/*").permitAll()
                .requestMatchers("/v3/api-docs").permitAll()

                //openvidu
                .requestMatchers(HttpMethod.POST,"/api/sessions").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/sessions/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/sessions/*/*").permitAll()
                .anyRequest().authenticated()

                .and()
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin().disable()
                .build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(tokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
