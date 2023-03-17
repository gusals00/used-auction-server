package com.auction.usedauction.test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Slf4j
public class ApiTestController {
    @GetMapping
    public ResponseEntity a(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().setAttribute("ss", "111");

        log.info("session id={}", request.getSession().getId());
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", request.getSession().getId())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
//                .maxAge(0)
                .path("/")
                .domain("bkkang1.github.io")
                .build();
        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(new Hello("hello",25));

    }

    @GetMapping("/AAA")
    public String result(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("ss") == null) {
            return "fail";
        }
        return "ok";
    }

    @Data
    static class Hello {
        private String name;
        private Integer age;

        public Hello(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

}
