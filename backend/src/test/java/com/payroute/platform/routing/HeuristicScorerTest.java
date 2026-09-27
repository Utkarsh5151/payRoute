package com.payroute.platform.routing;

import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import com.payroute.platform.routing.engine.HeuristicScorer;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicScorerTest {

    private HeuristicScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new HeuristicScorer();
    }

    @Test
    void testLatencySubscoreBoundaryConditions() {
        assertEquals(100.0, scorer.calculateLatencySubscore(50.0), 0.001);
        assertEquals(100.0, scorer.calculateLatencySubscore(100.0), 0.001);
        assertEquals(0.0, scorer.calculateLatencySubscore(2000.0), 0.001);
        assertEquals(0.0, scorer.calculateLatencySubscore(2500.0), 0.001);

        double mid = scorer.calculateLatencySubscore(1050.0);
        assertTrue(mid > 40.0 && mid < 60.0, "Expected mid-range latency subscore around 50");
    }

    @Test
    void testMethodAffinityBonus() {
        assertEquals(1.05, scorer.calculateMethodAffinity(PaymentMethod.UPI, "PROVIDER_A"), 0.001);
        assertEquals(1.00, scorer.calculateMethodAffinity(PaymentMethod.UPI, "PROVIDER_B"), 0.001);

        assertEquals(1.05, scorer.calculateMethodAffinity(PaymentMethod.CARD, "PROVIDER_B"), 0.001);
        assertEquals(1.00, scorer.calculateMethodAffinity(PaymentMethod.CARD, "PROVIDER_A"), 0.001);
    }

    @Test
    void testCircuitBreakerPenalties() {
        Payment payment = new Payment(
                new Merchant(), UUID.randomUUID(), new BigDecimal("100"), "USD",
                PaymentMethod.CARD, "alice@customer.dev", "desc", "ref", "key"
        );

        PaymentProvider provider = new PaymentProvider(
                "PROVIDER_A", "ApexPay", ProviderStatus.ACTIVE,
                new BigDecimal("98.00"), 200, new BigDecimal("1.00"), new BigDecimal("1.00"), 100
        );

        double closedScore = scorer.scoreProvider(payment, provider, "CLOSED");
        double halfOpenScore = scorer.scoreProvider(payment, provider, "HALF_OPEN");
        double openScore = scorer.scoreProvider(payment, provider, "OPEN");

        assertTrue(closedScore > 80.0, "Closed circuit breaker should score high");
        assertEquals(closedScore * 0.4, halfOpenScore, 0.01, "Half-open should be exactly 40% of closed score");
        assertEquals(0.0, openScore, 0.001, "Open circuit breaker score should be strictly zero");
    }
}
