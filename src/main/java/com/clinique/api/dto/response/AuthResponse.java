package com.clinique.api.dto.response;

import java.util.Set;

public record AuthResponse(
        String token,
        String type,
        long expiresIn,
        Long userId,
        String email,
        Set<String> roles
) {
    public static AuthResponse bearer(String token, long expiresIn, Long userId, String email, Set<String> roles) {
        return new AuthResponse(token, "Bearer", expiresIn, userId, email, roles);
    }
}
