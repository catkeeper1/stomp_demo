package com.example.messagingstompwebsocket;

import javax.security.auth.Subject;
import java.security.Principal;

public class SimpleUser implements Principal {

    private String name;

    public SimpleUser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
