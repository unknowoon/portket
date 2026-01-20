package com.portket.app.controller;

import com.portket.app.domain.User;
import com.portket.app.service.UserService;
import com.portket.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> user(@AuthenticationPrincipal CustomUserDetails principal) {
        User currentUser = userService.getCurrentUser();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", currentUser.getId());
        userInfo.put("name", currentUser.getName());
        userInfo.put("email", currentUser.getEmail());

        return ResponseEntity.ok(userInfo);
    }
}
