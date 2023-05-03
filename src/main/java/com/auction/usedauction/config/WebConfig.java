package com.auction.usedauction.config;

import com.auction.usedauction.interceptor.AuctionEndCheckInterceptor;
import com.auction.usedauction.repository.auction_end.AuctionEndRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuctionEndRepository auctionEndRepository;
    private final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuctionEndCheckInterceptor(auctionEndRepository))
                .order(1)
                .addPathPatterns("/api/auctions/**");

    }
}
