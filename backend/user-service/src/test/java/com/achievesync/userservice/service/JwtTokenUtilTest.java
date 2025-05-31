package com.achievesync.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        // Set a test secret
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", "test-secret-key-that-is-long-enough-for-hmac512-algorithm");
        
        userDetails = new User("test@example.com", "password", new ArrayList<>());
    }

    @Test
    void testGenerateToken() {
        String token = jwtTokenUtil.generateToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtTokenUtil.generateToken(userDetails);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        
        assertEquals("test@example.com", username);
    }

    @Test
    void testGetExpirationDateFromToken() {
        String token = jwtTokenUtil.generateToken(userDetails);
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtTokenUtil.generateToken(userDetails);
        Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUser() {
        String token = jwtTokenUtil.generateToken(userDetails);
        UserDetails differentUser = new User("different@example.com", "password", new ArrayList<>());
        
        Boolean isValid = jwtTokenUtil.validateToken(token, differentUser);
        
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // Create a token with very short expiration
        ReflectionTestUtils.setField(jwtTokenUtil, "JWT_TOKEN_VALIDITY", -1L); // Already expired
        
        String token = jwtTokenUtil.generateToken(userDetails);
        
        // Reset to normal expiration
        ReflectionTestUtils.setField(jwtTokenUtil, "JWT_TOKEN_VALIDITY", 5 * 60 * 60L);
        
        Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
        
        assertFalse(isValid);
    }

    @Test
    void testTokenConsistency() {
        // Generate multiple tokens for the same user
        String token1 = jwtTokenUtil.generateToken(userDetails);
        String token2 = jwtTokenUtil.generateToken(userDetails);
        
        // Tokens should be different (due to timestamp)
        assertNotEquals(token1, token2);
        
        // But both should be valid and contain same username
        assertEquals(jwtTokenUtil.getUsernameFromToken(token1), jwtTokenUtil.getUsernameFromToken(token2));
        assertTrue(jwtTokenUtil.validateToken(token1, userDetails));
        assertTrue(jwtTokenUtil.validateToken(token2, userDetails));
    }

    @Test
    void testInvalidTokenFormat() {
        String invalidToken = "invalid.token.format";
        
        assertThrows(Exception.class, () -> {
            jwtTokenUtil.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void testNullToken() {
        assertThrows(Exception.class, () -> {
            jwtTokenUtil.getUsernameFromToken(null);
        });
    }

    @Test
    void testEmptyToken() {
        assertThrows(Exception.class, () -> {
            jwtTokenUtil.getUsernameFromToken("");
        });
    }
}