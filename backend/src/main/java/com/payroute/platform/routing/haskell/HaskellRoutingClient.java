package com.payroute.platform.routing.haskell;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.routing.dto.HaskellCandidateDto;
import com.payroute.platform.routing.dto.HaskellRoutingDecision;
import com.payroute.platform.routing.dto.HaskellRoutingRequest;
import com.payroute.platform.routing.dto.RoutingDecision;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HaskellRoutingClient {

    private static final Logger log = LoggerFactory.getLogger(HaskellRoutingClient.class);

    private final WebClient haskellWebClient;

    public HaskellRoutingClient(@Qualifier("haskellWebClient") WebClient haskellWebClient) {
        this.haskellWebClient = haskellWebClient;
    }

    public Optional<RoutingDecision> routeWithHaskell(Payment payment, List<PaymentProvider> candidates,
                                                      Map<String, String> circuitBreakerStates) {
        try {
            Map<UUID, PaymentProvider> providerById = candidates.stream()
                    .collect(Collectors.toMap(PaymentProvider::getId, p -> p));

            List<HaskellCandidateDto> candidateDtos = candidates.stream()
                    .map(p -> new HaskellCandidateDto(
                            p.getId().toString(),
                            p.getCode(),
                            p.getName(),
                            p.getSuccessRate() != null ? p.getSuccessRate().doubleValue() : 95.0,
                            p.getAvgLatencyMs() != null ? p.getAvgLatencyMs().doubleValue() : 250.0,
                            p.getTimeoutRate() != null ? p.getTimeoutRate().doubleValue() : 2.0,
                            p.getFailureRate() != null ? p.getFailureRate().doubleValue() : 2.0,
                            circuitBreakerStates.getOrDefault(p.getCode(), "CLOSED"),
                            p.getPriorityWeight() != null ? p.getPriorityWeight() : 100
                    ))
                    .collect(Collectors.toList());

            HaskellRoutingRequest request = new HaskellRoutingRequest(
                    payment.getAmount().doubleValue(),
                    payment.getCurrency(),
                    payment.getPaymentMethod().name(),
                    candidateDtos
            );

            HaskellRoutingDecision response = haskellWebClient.post()
                    .uri("/route")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(HaskellRoutingDecision.class)
                    .block();

            if (response != null && response.getSelectedProviderId() != null) {
                UUID selectedId = UUID.fromString(response.getSelectedProviderId());
                PaymentProvider selected = providerById.get(selectedId);

                if (selected != null) {
                    List<PaymentProvider> ordered = new ArrayList<>();
                    ordered.add(selected);
                    for (PaymentProvider p : candidates) {
                        if (!p.getId().equals(selectedId)) {
                            ordered.add(p);
                        }
                    }

                    log.info("Haskell rules engine routed payment {} to {} with score {}",
                            payment.getId(), selected.getCode(), response.getCalculatedScore());

                    return Optional.of(new RoutingDecision(
                            selected,
                            ordered,
                            response.getCalculatedScore(),
                            "HASKELL_RULES_ENGINE",
                            response.getExplanation()
                    ));
                }
            }
        } catch (Exception ex) {
            log.warn("Haskell routing service unavailable or failed: {}. Falling back to internal Java Heuristic Scorer.", ex.getMessage());
        }

        return Optional.empty();
    }
}
