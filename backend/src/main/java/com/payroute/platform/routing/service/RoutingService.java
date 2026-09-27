package com.payroute.platform.routing.service;

import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import com.payroute.platform.provider.repository.PaymentProviderRepository;
import com.payroute.platform.routing.dto.RoutingDecision;
import com.payroute.platform.routing.engine.HeuristicScorer;
import com.payroute.platform.routing.haskell.HaskellRoutingClient;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final PaymentProviderRepository providerRepository;
    private final HeuristicScorer heuristicScorer;
    private final HaskellRoutingClient haskellRoutingClient;

    public RoutingService(PaymentProviderRepository providerRepository,
                          HeuristicScorer heuristicScorer,
                          HaskellRoutingClient haskellRoutingClient) {
        this.providerRepository = providerRepository;
        this.heuristicScorer = heuristicScorer;
        this.haskellRoutingClient = haskellRoutingClient;
    }

    @Transactional(readOnly = true)
    public RoutingDecision routePayment(Payment payment, Map<String, String> circuitBreakerStates) {
        List<PaymentProvider> activeProviders = providerRepository.findByStatus(ProviderStatus.ACTIVE);
        if (activeProviders.isEmpty()) {
            throw new BadRequestException("No active payment providers are available for routing");
        }

        Map<String, String> cbStates = circuitBreakerStates != null ? circuitBreakerStates : Collections.emptyMap();

        // Attempt routing via Haskell Rules Engine
        Optional<RoutingDecision> haskellDecision = haskellRoutingClient.routeWithHaskell(payment, activeProviders, cbStates);
        if (haskellDecision.isPresent()) {
            return haskellDecision.get();
        }

        // Fallback: Java Heuristic Engine
        log.info("Executing Java Heuristic Scorer for payment {}", payment.getId());

        List<ScoredCandidate> scoredList = new ArrayList<>();
        for (PaymentProvider p : activeProviders) {
            String cb = cbStates.getOrDefault(p.getCode(), "CLOSED");
            double score = heuristicScorer.scoreProvider(payment, p, cb);
            scoredList.add(new ScoredCandidate(p, score));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        List<PaymentProvider> ranked = scoredList.stream()
                .map(sc -> sc.provider)
                .collect(Collectors.toList());

        PaymentProvider best = ranked.get(0);
        double topScore = scoredList.get(0).score;

        String explanation = String.format(
                "Java Heuristic Engine selected %s (%s) with score %.1f based on success rate, latency, priority, and method affinity.",
                best.getName(), best.getCode(), topScore
        );

        return new RoutingDecision(best, ranked, topScore, "JAVA_HEURISTIC_FALLBACK", explanation);
    }

    private static class ScoredCandidate {
        final PaymentProvider provider;
        final double score;

        ScoredCandidate(PaymentProvider provider, double score) {
            this.provider = provider;
            this.score = score;
        }
    }
}
