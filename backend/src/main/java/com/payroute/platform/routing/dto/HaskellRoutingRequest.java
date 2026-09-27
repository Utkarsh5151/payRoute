package com.payroute.platform.routing.dto;

import java.util.List;

public class HaskellRoutingRequest {

    private double amount;
    private String currency;
    private String paymentMethod;
    private List<HaskellCandidateDto> candidates;

    public HaskellRoutingRequest() {
    }

    public HaskellRoutingRequest(double amount, String currency, String paymentMethod, List<HaskellCandidateDto> candidates) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.candidates = candidates;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<HaskellCandidateDto> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<HaskellCandidateDto> candidates) {
        this.candidates = candidates;
    }
}
