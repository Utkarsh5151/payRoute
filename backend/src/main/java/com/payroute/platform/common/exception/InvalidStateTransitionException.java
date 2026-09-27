package com.payroute.platform.common.exception;

public class InvalidStateTransitionException extends RuntimeException {

    private final String fromState;
    private final String toState;

    public InvalidStateTransitionException(String fromState, String toState) {
        super(String.format("Invalid state transition from [%s] to [%s]", fromState, toState));
        this.fromState = fromState;
        this.toState = toState;
    }

    public String getFromState() {
        return fromState;
    }

    public String getToState() {
        return toState;
    }
}
