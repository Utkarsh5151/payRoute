package com.payroute.platform.provider.mock;

import org.springframework.stereotype.Component;

@Component("PROVIDER_C")
public class MockProviderC extends AbstractMockProviderClient {

    @Override
    public String getProviderCode() {
        return "PROVIDER_C";
    }
}
