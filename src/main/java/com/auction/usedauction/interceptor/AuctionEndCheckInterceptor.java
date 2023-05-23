package com.auction.usedauction.interceptor;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.repository.auction_end.AuctionEndRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class AuctionEndCheckInterceptor implements HandlerInterceptor {

    private final AuctionEndRepository auctionEndRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (method.equals(HttpMethod.POST)) {

            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String pathVariable = (String) pathVariables.get("auctionId");
            log.info("REQUEST URL = {}, pathVariable = {}", request.getRequestURL(), pathVariable);
            Long auctionId = Long.valueOf(pathVariable);

            LocalDateTime auctionEndDate = auctionEndRepository.findByAuctionId(auctionId);
            if (auctionEndDate == null) {
                log.info("정해진 기간 내에 경매 종료 시간이 해당되지 않는 auctionId 입니다.");
                return true;
            }
            if (auctionEndDate.isBefore(LocalDateTime.now())) {
                log.info("경매 종료 시간이 지났습니다.");
                throw new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING);
            }
        }

        return true;
    }
}
