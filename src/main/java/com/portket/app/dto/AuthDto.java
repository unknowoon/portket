package com.portket.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthDto {
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleLoginInfo {
        private String loginUrl;
        private String clientId;
    }
    
    @Getter
    @Builder
    public static class TokenRefreshRequest {
        // 리프레시 토큰은 쿠키에서 자동으로 읽어오므로 별도 필드 불필요
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRefreshResponse {
        private boolean success;
        private String message;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        private boolean success;
        private String message;
    }
}