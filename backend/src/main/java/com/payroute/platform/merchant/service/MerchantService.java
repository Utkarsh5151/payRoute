package com.payroute.platform.merchant.service;

import com.payroute.platform.auth.entity.User;
import com.payroute.platform.common.exception.ResourceNotFoundException;
import com.payroute.platform.common.util.HashUtils;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.merchant.repository.MerchantRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public Merchant createMerchant(User user, String name, String webhookUrl) {
        String rawApiKey = generateRawApiKey();
        String apiKeyHash = HashUtils.sha256(rawApiKey);

        Merchant merchant = new Merchant(user, name != null ? name : user.getUsername() + "'s Store", apiKeyHash, webhookUrl);
        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public Merchant getMerchantByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public Optional<Merchant> findByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Merchant> findByApiKey(String rawApiKey) {
        String hash = HashUtils.sha256(rawApiKey);
        return merchantRepository.findByApiKeyHash(hash);
    }

    @Transactional(readOnly = true)
    public Merchant getById(UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + merchantId));
    }

    private String generateRawApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "pr_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
