package com.portket.app.controller;

import com.portket.app.dto.AuthDto;
import com.portket.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final JwtUtil jwtUtil;
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(summary = "구글 로그인 정보 조회", 
              description = "프론트엔드에서 구글 OAuth2 로그인을 시작하기 위한 정보를 제공합니다")
    @GetMapping("/google")
    public ResponseEntity<AuthDto.GoogleLoginInfo> getGoogleLoginInfo() {
        return ResponseEntity.ok(
            AuthDto.GoogleLoginInfo.builder()
                .loginUrl("/oauth2/authorization/google")
                .clientId(googleClientId)
                .build()
        );
    }

    @Operation(summary = "토큰 갱신", 
              description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenRefreshResponse> refreshToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = extractTokenFromCookie(request, "refresh_token");
            
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDto.TokenRefreshResponse.builder()
                        .success(false)
                        .message("Refresh token not found")
                        .build());
            }
            
            // 리프레시 토큰 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDto.TokenRefreshResponse.builder()
                        .success(false)
                        .message("Invalid refresh token")
                        .build());
            }

            // 리프레시 토큰에서 이메일 추출
            String userEmail = jwtUtil.extractEmail(refreshToken);
            
            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtUtil.generateAccessToken(userEmail);

            // 새 액세스 토큰을 쿠키에 설정
            Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(cookieSecure);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(24 * 60 * 60); // 1일
            accessTokenCookie.setAttribute("SameSite", "Strict");
            
            response.addCookie(accessTokenCookie);

            log.info("Access token refreshed for user: {}", userEmail);
            
            return ResponseEntity.ok(
                AuthDto.TokenRefreshResponse.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthDto.TokenRefreshResponse.builder()
                    .success(false)
                    .message("Token refresh failed")
                    .build());
        }
    }

    @Operation(summary = "로그아웃", 
              description = "쿠키에서 토큰을 삭제하여 로그아웃 처리합니다")
    @PostMapping("/logout")
    public ResponseEntity<AuthDto.LogoutResponse> logout(HttpServletResponse response) {
        // 액세스 토큰 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("access_token", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 즉시 만료
        accessTokenCookie.setAttribute("SameSite", "Strict");

        // 리프레시 토큰 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh_token", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 즉시 만료
        refreshTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("User logged out successfully");

        return ResponseEntity.ok(
            AuthDto.LogoutResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build()
        );
    }

    /**
     * HttpServletRequest에서 특정 이름의 쿠키 값을 추출하는 헬퍼 메서드
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}