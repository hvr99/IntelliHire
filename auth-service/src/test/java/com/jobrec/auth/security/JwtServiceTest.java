package com.jobrec.auth.security;

import com.jobrec.auth.domain.entity.User;
import com.jobrec.auth.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void generateToken_claimsAreCorrect() {
        JwtService jwtService = new JwtService(jwtEncoder, 60);
        User user = new User(UUID.randomUUID(), "test@test.com", "hash", Role.RECRUITER, true, LocalDateTime.now());
        
        Jwt mockJwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("mock-token")
            .header("alg", "HS256")
            .claim("sub", user.getId().toString())
            .build();
            
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);
        
        String token = jwtService.generateToken(user);
        assertEquals("mock-token", token);
    }
}
