package com.example.medhub.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    ROLE_ADMIN, ROLE_WORKER, ROLE_USER;
    @Override
    public String getAuthority() {
        return name();
    }
}