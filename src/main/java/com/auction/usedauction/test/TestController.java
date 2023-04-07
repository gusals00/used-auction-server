package com.auction.usedauction.test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping(value = "/api/member/signup",method = RequestMethod.OPTIONS)
    public ResponseEntity<String> preFlightHandler(){
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "https://bkkang1.github.io");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body("pleaseeeeeeeeeeee");
    }
}
