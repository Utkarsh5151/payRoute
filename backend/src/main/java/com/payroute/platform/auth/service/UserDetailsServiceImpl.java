package com.payroute.platform.auth.service;

import com.payroute.platform.auth.entity.User;
import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.auth.repository.UserRepository;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.merchant.repository.MerchantRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, MerchantRepository merchantRepository) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        UUID merchantId = merchantRepository.findByUserId(user.getId())
                .map(Merchant::getId)
                .orElse(null);

        return UserPrincipal.create(user, merchantId);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        UUID merchantId = merchantRepository.findByUserId(user.getId())
                .map(Merchant::getId)
                .orElse(null);

        return UserPrincipal.create(user, merchantId);
    }
}
