package com.payroute.platform.provider.mock;

import org.springframework.stereotype.Component;

@Component("PROVIDER_A")
public class MockProviderA extends AbstractMockProviderClient {

    @Override
    public String getProviderCode() {
        return "PROVIDER_A";
    }
}
