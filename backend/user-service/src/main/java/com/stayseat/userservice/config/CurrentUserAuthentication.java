package com.stayseat.userservice.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class CurrentUserAuthentication extends AbstractAuthenticationToken {

    private final CurrentUser currentUser;

    public CurrentUserAuthentication(CurrentUser currentUser) {
        super(authorities(currentUser));
        this.currentUser = currentUser;
        setAuthenticated(true);
    }

    private static List<GrantedAuthority> authorities(CurrentUser currentUser) {
        if (currentUser.role() == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + currentUser.role().toUpperCase()));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return currentUser;
    }
}
