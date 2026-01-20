package com.portket.app.service;

import com.portket.app.domain.User;
import com.portket.app.repository.UserRepository;
import com.portket.exception.BizException;
import com.portket.util.system.SecurityUtils;
import com.portket.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createUser();
        testUser.setId(1L);
    }
    
    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // when
        User found = userService.findById(1L);
        
        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("ID로 사용자 조회 실패 - 사용자 없음")
    void findById_NotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id");
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // when
        User found = userService.findByEmail("test@example.com");
        
        // then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 사용자 없음")
    void findByEmail_NotFound() {
        // given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.findByEmail("notfound@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with email");
    }
    
    @Test
    @DisplayName("사용자 이름 업데이트 성공")
    void updateUserName_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            
            // when
            User result = userService.updateUserName("Updated Name");
            
            // then
            assertThat(result).isNotNull();
            verify(userRepository, times(1)).save(any(User.class));
        }
    }
    
    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            
            // when
            User currentUser = userService.getCurrentUser();
            
            // then
            assertThat(currentUser).isNotNull();
            assertThat(currentUser.getEmail()).isEqualTo("test@example.com");
        }
    }
}