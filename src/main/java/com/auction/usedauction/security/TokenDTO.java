package com.auction.usedauction.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TokenDTO {

    @NotEmpty
    private String accessToken;

    @NotEmpty
    private String refreshToken;
}
