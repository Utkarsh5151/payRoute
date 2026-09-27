package com.payroute.platform.auth.service;

import com.payroute.platform.auth.dto.AuthResponse;
import com.payroute.platform.auth.dto.LoginRequest;
import com.payroute.platform.auth.dto.RegisterRequest;
import com.payroute.platform.auth.dto.UserDto;
import com.payroute.platform.auth.entity.Role;
import com.payroute.platform.auth.entity.User;
import com.payroute.platform.auth.jwt.JwtTokenProvider;
import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.auth.repository.UserRepository;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.SecurityUtils;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.merchant.service.MerchantService;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final MerchantService merchantService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       MerchantService merchantService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.merchantService = merchantService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.MERCHANT;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getUsername(), request.getEmail().toLowerCase().trim(), encodedPassword, role);
        user = userRepository.save(user);

        UUID merchantId = null;
        if (role == Role.MERCHANT || role == Role.ADMIN) {
            Merchant merchant = merchantService.createMerchant(user, request.getBusinessName(), null);
            merchantId = merchant.getId();
        }

        UserPrincipal principal = UserPrincipal.create(user, merchantId);
        String token = tokenProvider.generateToken(principal);

        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), merchantId);
        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal);

        UserDto userDto = new UserDto(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRole(),
                principal.getMerchantId()
        );

        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userDto);
    }

    public UserDto getCurrentUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("No authenticated user found"));

        return new UserDto(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRole(),
                principal.getMerchantId()
        );
    }
}
