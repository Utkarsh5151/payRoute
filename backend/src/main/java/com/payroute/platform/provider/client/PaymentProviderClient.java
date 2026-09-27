package com.payroute.platform.provider.client;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.provider.entity.PaymentProvider;

public interface PaymentProviderClient {

    String getProviderCode();

    ProviderResult executePayment(Payment payment, PaymentProvider config);
}
