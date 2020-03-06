package com.example.amazone_ecommerce.Model;

public class Token {
    public String token;
    public boolean isServiceToken;

    public Token() {
    }

    public Token(String token, boolean isServiceToken) {
        this.token = token;
        this.isServiceToken = isServiceToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServiceToken() {
        return isServiceToken;
    }

    public void setServiceToken(boolean serviceToken) {
        isServiceToken = serviceToken;
    }
}