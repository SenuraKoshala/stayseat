package com.stayseat.paymentservice.config;

import com.stayseat.paymentservice.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtil {

    private AuthUtil() {}

    public static CurrentUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentUser user)) {
            throw ApiException.unauthenticated("Authentication required.");
        }
        return user;
    }

    /** True if the current user is the resource owner, or a SYSTEM_ADMIN. */
    public static boolean isOwnerOrAdmin(CurrentUser user, java.util.UUID ownerId) {
        return user.hasRole("SYSTEM_ADMIN") || user.userId().equals(ownerId);
    }
}
