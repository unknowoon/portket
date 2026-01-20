package com.portket.app.controller;

import com.portket.app.domain.User;
import com.portket.app.service.UserService;
import com.portket.security.jwt.JwtAuthenticationFilter;
import com.portket.security.jwt.JwtUtil;
import com.portket.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(UserController.class)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@DisplayName("UserController 테스트")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createUser();
        testUser.setId(1L);
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("현재 사용자 정보 조회 성공")
    void getCurrentUser_Success() throws Exception {
        // given
        when(userService.getCurrentUser()).thenReturn(testUser);
        
        // when & then
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
        
        verify(userService, times(1)).getCurrentUser();
    }
    
    @Test
    @DisplayName("현재 사용자 정보 조회 실패 - 인증 없음")
    void getCurrentUser_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        
        verify(userService, never()).getCurrentUser();
    }
}