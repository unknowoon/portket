package com.portket.security;

import com.portket.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil 테스트")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private final String testSecret = "testSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKey";
    private final Long testExpirationTime = 3600000L; // 1시간
    private final Long testRefreshExpirationTime = 86400000L; // 24시간
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", testExpirationTime);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationTime", testRefreshExpirationTime);
        jwtUtil.initSecretKey();
    }
    
    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void generateToken_Success() {
        // given
        String username = "test@example.com";
        
        // when
        String token = jwtUtil.generateToken(username);
        
        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
    }
    
    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void generateRefreshToken_Success() {
        // given
        String username = "test@example.com";
        
        // when
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }
    
    @Test
    @DisplayName("토큰에서 사용자 이름 추출 성공")
    void extractUsername_Success() {
        // given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when
        String extractedUsername = jwtUtil.extractUsername(token);
        
        // then
        assertThat(extractedUsername).isEqualTo(username);
    }
    
    @Test
    @DisplayName("토큰에서 Claims 추출 성공")
    void extractAllClaims_Success() {
        // given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when
        Claims claims = jwtUtil.extractAllClaims(token);
        
        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }
    
    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void isTokenValid_Success() {
        // given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when
        boolean isValid = jwtUtil.isTokenValid(token, username);
        
        // then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("토큰 검증 실패 - 다른 사용자")
    void isTokenValid_DifferentUser() {
        // given
        String username = "test@example.com";
        String otherUsername = "other@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when
        boolean isValid = jwtUtil.isTokenValid(token, otherUsername);
        
        // then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("토큰 검증 실패 - 만료된 토큰")
    void isTokenValid_ExpiredToken() {
        // given
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", -1000L); // 이미 만료됨
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationTime", testRefreshExpirationTime);
        jwtUtil.initSecretKey();
        
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when & then
        assertThatThrownBy(() -> jwtUtil.isTokenValid(token, username))
                .isInstanceOf(ExpiredJwtException.class);
    }
    
    @Test
    @DisplayName("토큰 검증 실패 - 잘못된 형식")
    void isTokenValid_MalformedToken() {
        // given
        String malformedToken = "invalid.token.format";
        
        // when & then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }
    
    @Test
    @DisplayName("토큰 만료 여부 확인 - 만료되지 않음")
    void isTokenExpired_NotExpired() {
        // given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);
        
        // when
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // then
        assertThat(isExpired).isFalse();
    }
    
    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰의 만료 시간이 다름")
    void tokenExpirationTimes_Different() {
        // given
        String username = "test@example.com";
        String accessToken = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        // when
        Claims accessClaims = jwtUtil.extractAllClaims(accessToken);
        Claims refreshClaims = jwtUtil.extractAllClaims(refreshToken);
        
        long accessExpTime = accessClaims.getExpiration().getTime() - accessClaims.getIssuedAt().getTime();
        long refreshExpTime = refreshClaims.getExpiration().getTime() - refreshClaims.getIssuedAt().getTime();
        
        // then
        assertThat(accessExpTime).isLessThan(refreshExpTime);
        assertThat(accessExpTime).isCloseTo(testExpirationTime, within(1000L));
        assertThat(refreshExpTime).isCloseTo(testRefreshExpirationTime, within(1000L));
    }
}