package com.payroute.platform.provider.mock;

import org.springframework.stereotype.Component;

@Component("PROVIDER_B")
public class MockProviderB extends AbstractMockProviderClient {

    @Override
    public String getProviderCode() {
        return "PROVIDER_B";
    }
}
