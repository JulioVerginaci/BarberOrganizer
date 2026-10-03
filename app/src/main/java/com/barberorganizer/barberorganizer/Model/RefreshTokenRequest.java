package com.barberorganizer.barberorganizer.Model;

public class RefreshTokenRequest {

    private String refresh_token;

    public RefreshTokenRequest(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    public String getRefresh_token() {
        return refresh_token;
    }
}