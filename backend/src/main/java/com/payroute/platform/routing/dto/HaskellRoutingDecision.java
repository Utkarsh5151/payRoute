package com.payroute.platform.routing.dto;

import java.util.List;

public class HaskellRoutingDecision {

    private String selectedProviderId;
    private String selectedProviderCode;
    private String selectedProviderName;
    private double calculatedScore;
    private List<Object> rankedCandidates;
    private String explanation;

    public HaskellRoutingDecision() {
    }

    public String getSelectedProviderId() {
        return selectedProviderId;
    }

    public void setSelectedProviderId(String selectedProviderId) {
        this.selectedProviderId = selectedProviderId;
    }

    public String getSelectedProviderCode() {
        return selectedProviderCode;
    }

    public void setSelectedProviderCode(String selectedProviderCode) {
        this.selectedProviderCode = selectedProviderCode;
    }

    public String getSelectedProviderName() {
        return selectedProviderName;
    }

    public void setSelectedProviderName(String selectedProviderName) {
        this.selectedProviderName = selectedProviderName;
    }

    public double getCalculatedScore() {
        return calculatedScore;
    }

    public void setCalculatedScore(double calculatedScore) {
        this.calculatedScore = calculatedScore;
    }

    public List<Object> getRankedCandidates() {
        return rankedCandidates;
    }

    public void setRankedCandidates(List<Object> rankedCandidates) {
        this.rankedCandidates = rankedCandidates;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
