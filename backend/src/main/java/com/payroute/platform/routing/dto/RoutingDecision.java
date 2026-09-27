package com.payroute.platform.routing.dto;

import com.payroute.platform.provider.entity.PaymentProvider;
import java.util.List;

public class RoutingDecision {

    private final PaymentProvider selectedProvider;
    private final List<PaymentProvider> rankedProviders;
    private final double score;
    private final String engineSource; // e.g. "HASKELL_RULES_ENGINE" or "JAVA_HEURISTIC_FALLBACK"
    private final String explanation;

    public RoutingDecision(PaymentProvider selectedProvider, List<PaymentProvider> rankedProviders,
                           double score, String engineSource, String explanation) {
        this.selectedProvider = selectedProvider;
        this.rankedProviders = rankedProviders;
        this.score = score;
        this.engineSource = engineSource;
        this.explanation = explanation;
    }

    public PaymentProvider getSelectedProvider() {
        return selectedProvider;
    }

    public List<PaymentProvider> getRankedProviders() {
        return rankedProviders;
    }

    public double getScore() {
        return score;
    }

    public String getEngineSource() {
        return engineSource;
    }

    public String getExplanation() {
        return explanation;
    }
}
