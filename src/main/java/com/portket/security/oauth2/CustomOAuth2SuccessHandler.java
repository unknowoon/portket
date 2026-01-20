package com.portket.security.oauth2;

import com.portket.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Setter
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authRequestRepository;
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public CustomOAuth2SuccessHandler(JwtUtil jwtUtil,
                                      AuthorizationRequestRepository<OAuth2AuthorizationRequest> authRequestRepository) {
        this.jwtUtil = jwtUtil;
        this.authRequestRepository = authRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 세션에서 OAuth2AuthorizationRequest 객체 제거하며 가져오기
        OAuth2AuthorizationRequest oAuth2Request =
                authRequestRepository.removeAuthorizationRequest(request, response);

        // JWT 생성 로직: OAuth2User로부터 이메일 획득 후 토큰 생성
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 액세스 토큰(짧게), 리프레시 토큰(길게) 생성
        String accessToken = jwtUtil.generateToken(email);         // 예: 15분 ~ 1시간
        String refreshToken = jwtUtil.generateRefreshToken(email); // 예: 7일

        // -------------------------------------------------------
        // 1) 액세스 토큰 - HttpOnly = true로 변경 (보안 강화)
        //    -> XSS 공격 방지
        //    -> 필요시 별도 API 엔드포인트를 통해 토큰 조회
        // -------------------------------------------------------
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);           // <== 보안 강화
        accessTokenCookie.setSecure(cookieSecure);    // 환경에 따라 설정
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(24 * 60 * 60); // 1일
        accessTokenCookie.setAttribute("SameSite", "Strict"); // CSRF 방지
        response.addCookie(accessTokenCookie);

        // -------------------------------------------------------
        // 2) 리프레시 토큰 - HttpOnly = true
        //    -> XSS로부터 안전, 자동 전송되므로 CSRF 방어도 함께 고려
        //    -> 비교적 긴 만료 시간 (일주일 이상)
        // -------------------------------------------------------
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);          // <== XSS 보호
        refreshTokenCookie.setSecure(cookieSecure);   // 환경에 따라 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        refreshTokenCookie.setAttribute("SameSite", "Strict"); // CSRF 방지
        response.addCookie(refreshTokenCookie);

        // 원래 가려던 URL 가져오기 (저장된 redirectUri 또는 기본 URL)
        String redirectUrl = getRedirectUrl(request, oAuth2Request);
        response.sendRedirect(redirectUrl);
    }

    private String getRedirectUrl(HttpServletRequest request,
                                         OAuth2AuthorizationRequest oAuth2Request) {
        String redirectUrl = frontendUrl; // 환경 변수로부터 기본값 설정

        // 세션에 저장된 redirect_uri 가져오기
        String sessionRedirectUri = (String) request.getSession().getAttribute("redirect_uri");
        if (sessionRedirectUri != null) {
            return sessionRedirectUri;
        }

        if (oAuth2Request != null) {
            String savedRedirectUri = oAuth2Request.getRedirectUri();
            if (savedRedirectUri != null) {
                redirectUrl = savedRedirectUri;
            }
        }

        return redirectUrl;
    }
}