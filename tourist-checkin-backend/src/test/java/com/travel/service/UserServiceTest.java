package com.travel.service;

import com.travel.dto.UserLoginDTO;
import com.travel.entity.User;
import com.travel.exception.UnauthorizedException;
import com.travel.mapper.UserMapper;
import com.travel.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void login_withInvalidPasswordHash_shouldThrowUnauthorizedException_not500() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setAccount("test");
        dto.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setAccount("test");
        user.setUsername("test");
        user.setPassword(".zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO");

        when(userMapper.selectOne(any())).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> userService.login(dto));
    }

    @Test
    void login_withWrongPassword_shouldThrowUnauthorizedException() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setAccount("test");
        dto.setPassword("wrongpass");

        User user = new User();
        user.setId(1L);
        user.setAccount("test");
        user.setUsername("test");
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));

        when(userMapper.selectOne(any())).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> userService.login(dto));
    }

    @Test
    void login_withCorrectPassword_shouldReturnToken() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setAccount("test");
        dto.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setAccount("test");
        user.setUsername("test");
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
        user.setRole("USER");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtUtil.generateToken(1L, "test", "USER")).thenReturn("mock-jwt-token");

        String token = userService.login(dto);

        assertEquals("mock-jwt-token", token);
    }
}
