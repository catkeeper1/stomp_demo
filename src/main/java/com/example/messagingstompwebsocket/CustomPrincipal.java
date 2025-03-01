package com.example.messagingstompwebsocket;

import java.security.Principal;

public class CustomPrincipal implements Principal {
    private final String name;
    private final String customAttribute;

    public CustomPrincipal(String name, String customAttribute) {
        this.name = name;
        this.customAttribute = customAttribute;
    }

    public String getCustomAttribute() {
        return customAttribute;
    }

    @Override
    public String getName() {
        return name; // 或者返回其他标识
    }
}
