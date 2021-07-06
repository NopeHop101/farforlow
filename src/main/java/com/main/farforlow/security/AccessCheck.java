package com.main.farforlow.security;

import org.springframework.stereotype.Component;

@Component(value = "accessCheck")
public class AccessCheck {

    private final String headerToken = System.getenv("APP_TOKEN");

    public boolean check(String token) {
        return token != null && token.equals(headerToken);
    }
}
