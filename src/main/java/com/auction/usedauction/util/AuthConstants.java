package com.auction.usedauction.util;

public interface AuthConstants {
    //jwt
    String AUTH_HEADER = "Authorization";
    String TOKEN_TYPE = "BEARER";

    //email
    String EMAIL = "Email";
    String EMAIL_AUTH = "EmailAuth";
    String EMAIL_AUTH_CODE = "EmailAuthCode";
    int EMAIL_AUTH_LIMIT = 180;
}
