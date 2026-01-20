package com.portket.app.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponse {
    
    @Getter
    @Builder
    public static class GoogleConfig {
        private String clientId;
        private String redirectUri;
    }
    
    @Getter
    @Builder
    public static class TokenRefresh {
        private boolean success;
        private String message;
    }
    
    @Getter
    @Builder
    public static class Logout {
        private boolean success;
        private String message;
    }
}