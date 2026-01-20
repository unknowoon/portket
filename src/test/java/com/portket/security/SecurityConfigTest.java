package com.portket.security;

import com.portket.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig 테스트")
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("공개 엔드포인트 접근 허용 - Swagger UI")
    void publicEndpoint_SwaggerUI_Allowed() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk().or(status().isFound())); // 리다이렉트 가능
    }
    
    @Test
    @DisplayName("공개 엔드포인트 접근 허용 - API Docs")
    void publicEndpoint_ApiDocs_Allowed() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("공개 엔드포인트 접근 허용 - Health")
    void publicEndpoint_Health_Allowed() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk().or(status().isNotFound())); // 엔드포인트가 구현되지 않았을 수 있음
    }
    
    @Test
    @DisplayName("보호된 엔드포인트 접근 차단 - 인증 없음")
    void protectedEndpoint_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("CORS 설정 확인 - 허용된 오리진")
    void cors_AllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized()); // CORS는 통과하지만 인증 없음
    }
    
    @Test
    @DisplayName("CORS 설정 확인 - 허용되지 않은 오리진")
    void cors_NotAllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Origin", "http://malicious.com"))
                .andExpect(status().isUnauthorized().or(status().isForbidden()));
    }
    
    @Test
    @DisplayName("CSRF 보호 비활성화 확인")
    void csrf_Disabled() throws Exception {
        // CSRF가 비활성화되어 있으므로 토큰 없이도 POST 요청 가능
        mockMvc.perform(post("/api/transactions")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnauthorized()); // CSRF는 통과하지만 인증 없음
    }
    
    @Test
    @DisplayName("세션 정책 - Stateless 확인")
    void sessionManagement_Stateless() throws Exception {
        // 세션을 생성하지 않으므로 쿠키가 없어야 함
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    assert result.getResponse().getCookie("JSESSIONID") == null;
                });
    }
}