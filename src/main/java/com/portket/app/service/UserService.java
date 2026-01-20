package com.portket.app.service;

import com.portket.app.domain.User;
import com.portket.app.repository.UserRepository;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        return SecurityUtils.getCurrentUserOrThrow();
    }
    
    @Transactional
    public User updateUserName(String newName) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        user.changeName(newName);
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
}