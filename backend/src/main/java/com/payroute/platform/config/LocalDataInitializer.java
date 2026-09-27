package com.payroute.platform.config;

import com.payroute.platform.auth.entity.Role;
import com.payroute.platform.auth.entity.User;
import com.payroute.platform.auth.repository.UserRepository;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.merchant.repository.MerchantRepository;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import com.payroute.platform.provider.repository.PaymentProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LocalDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataInitializer.class);

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalDataInitializer(UserRepository userRepository,
                                MerchantRepository merchantRepository,
                                PaymentProviderRepository providerRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking seed data for local environment...");

        // 1. Seed Payment Providers if none exist
        if (providerRepository.count() == 0) {
            log.info("Seeding default Payment Providers...");
            providerRepository.save(new PaymentProvider(
                    "PROVIDER_A", "ApexPay Gateway", ProviderStatus.ACTIVE,
                    new BigDecimal("98.00"), 200, new BigDecimal("1.00"), new BigDecimal("1.00"), 100));

            providerRepository.save(new PaymentProvider(
                    "PROVIDER_B", "NovaPay Switch", ProviderStatus.ACTIVE,
                    new BigDecimal("95.00"), 150, new BigDecimal("3.00"), new BigDecimal("2.00"), 90));

            providerRepository.save(new PaymentProvider(
                    "PROVIDER_C", "PulsePay Connect", ProviderStatus.ACTIVE,
                    new BigDecimal("90.00"), 350, new BigDecimal("5.00"), new BigDecimal("5.00"), 80));
        }

        // 2. Seed Default Admin User
        if (!userRepository.existsByUsername("admin")) {
            log.info("Seeding default Admin user...");
            User admin = new User("admin", "admin@payroute.dev", passwordEncoder.encode("Admin@123456"), Role.ADMIN);
            userRepository.save(admin);
        }

        // 3. Seed Default Merchant User & Entity
        if (!userRepository.existsByUsername("merchant_apex")) {
            log.info("Seeding default Merchant user and entity...");
            User merchantUser = new User("merchant_apex", "merchant@apex.dev", passwordEncoder.encode("Merchant@123456"), Role.MERCHANT);
            merchantUser = userRepository.save(merchantUser);

            Merchant merchant = new Merchant(
                    merchantUser,
                    "Apex Retail Solutions",
                    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    "https://webhook.site/payroute-demo"
            );
            merchantRepository.save(merchant);
        }

        // 4. Seed Default Customer User
        if (!userRepository.existsByUsername("customer_alice")) {
            log.info("Seeding default Customer user...");
            User customer = new User("customer_alice", "alice@customer.dev", passwordEncoder.encode("User@123456"), Role.USER);
            userRepository.save(customer);
        }

        log.info("Seed data initialization complete.");
    }
}
