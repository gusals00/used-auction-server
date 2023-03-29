package com.auction.usedauction.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;

import static com.auction.usedauction.util.AuthConstants.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String id;

    public String sendMail(String email, HttpSession session) throws MessagingException, UnsupportedEncodingException {
        String code = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setFrom(new InternetAddress(id+"@naver.com","kumoh_auction"));
        mimeMessageHelper.setSubject("[중고 경매] 회원가입 인증 코드");
        mimeMessageHelper.setText(createText(code), true);

        javaMailSender.send(mimeMessage);

        session.setAttribute(EMAIL_AUTH, Map.of(EMAIL, email, EMAIL_AUTH_CODE, code));
        session.setMaxInactiveInterval(EMAIL_AUTH_LIMIT); // 3분 제한 설정

        return code;
    }

    public boolean isAuthenticated(HttpSession session, String email, String code) {
        Map<String, String> map = (Map<String, String>) session.getAttribute(EMAIL_AUTH);
        return !map.isEmpty() && verify(map, email, code);
    }

    private boolean verify(Map<String, String> map, String email, String code) {
        return map.get(EMAIL).equals(email) && map.get(EMAIL_AUTH_CODE).equals(code);
    }

    public String createCode() {
        Random r = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = r.nextInt(4);

            switch (index) {
                case 0:
                    key.append((char) ((int) r.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) ((int) r.nextInt(26) + 65));
                    break;
                default:
                    key.append(r.nextInt(9));
            }
        }
        return key.toString();
    }

    public String createText(String code) {
        String text="";
        text += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">중고 경매</h1>";
        text += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 회원가입 화면에서 입력해주세요.</p>";
        text += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        text += code;
        text += "</td></tr></tbody></table></div>";

        return text;
    }
}
