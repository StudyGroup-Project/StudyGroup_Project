package com.study.focus.account.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}