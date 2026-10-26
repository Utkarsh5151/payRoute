package com.payroute.platform.auth;

import com.payroute.platform.auth.dto.RegisterRequest;
import com.payroute.platform.auth.entity.Role;
import com.payroute.platform.auth.repository.UserRepository;
import com.payroute.platform.auth.jwt.JwtTokenProvider;
import com.payroute.platform.auth.service.AuthService;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.merchant.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {
    @Mock UserRepository users;
    @Mock MerchantService merchants;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider jwt;

    @Test
    void publicRegistrationCannotCreateAdmin() {
        AuthService service = new AuthService(users, merchants, passwordEncoder, authenticationManager, jwt);
        RegisterRequest request = new RegisterRequest("attacker", "attacker@example.test",
                "password123", Role.ADMIN, "Unapproved");
        assertThrows(BadRequestException.class, () -> service.register(request));
        verifyNoInteractions(passwordEncoder, merchants);
    }
}
