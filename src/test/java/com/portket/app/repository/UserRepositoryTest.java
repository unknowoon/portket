package com.portket.app.repository;

import com.portket.app.domain.User;
import com.portket.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createUser();
        entityManager.persist(testUser);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // when
        Optional<User> found = userRepository.findById(testUser.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }
    
    @Test
    @DisplayName("ID로 사용자 조회 실패 - 존재하지 않는 ID")
    void findById_NotFound() {
        // when
        Optional<User> found = userRepository.findById(999L);
        
        // then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 이메일")
    void findByEmail_NotFound() {
        // when
        Optional<User> found = userRepository.findByEmail("notfound@example.com");
        
        // then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("사용자 저장 성공")
    void save_Success() {
        // given
        User newUser = TestDataBuilder.createUser("new@example.com", "New User");
        
        // when
        User saved = userRepository.save(newUser);
        entityManager.flush();
        
        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getName()).isEqualTo("New User");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("사용자 수정 성공")
    void update_Success() {
        // given
        testUser.changeName("Updated Name");
        
        // when
        User saved = userRepository.save(testUser);
        entityManager.flush();
        
        // then
        assertThat(saved.getName()).isEqualTo("Updated Name");
    }
    
    @Test
    @DisplayName("사용자 삭제 성공")
    void delete_Success() {
        // when
        userRepository.delete(testUser);
        entityManager.flush();
        
        // then
        Optional<User> found = userRepository.findById(testUser.getId());
        assertThat(found).isEmpty();
    }
}