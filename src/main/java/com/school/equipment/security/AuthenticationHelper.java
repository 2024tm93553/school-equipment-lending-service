package com.school.equipment.security;

import org.springframework.security.core.Authentication;

public class AuthenticationHelper {

    /**
     * Safely extracts the user ID from the Authentication object.
     * Returns null if the user ID cannot be extracted.
     */
    public static Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        Object details = authentication.getDetails();
        
        if (details instanceof Long) {
            return (Long) details;
        }
        
        // In case authentication details was not set correctly
        return null;
    }
}
