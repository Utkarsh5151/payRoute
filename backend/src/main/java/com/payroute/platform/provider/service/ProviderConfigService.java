package com.payroute.platform.provider.service;

import com.payroute.platform.common.exception.ResourceNotFoundException;
import com.payroute.platform.provider.client.PaymentProviderClient;
import com.payroute.platform.provider.dto.ProviderResponseDto;
import com.payroute.platform.provider.dto.UpdateProviderConfigRequest;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import com.payroute.platform.provider.repository.PaymentProviderRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderConfigService {

    private final PaymentProviderRepository repository;
    private final Map<String, PaymentProviderClient> clientMap;

    public ProviderConfigService(PaymentProviderRepository repository,
                                 List<PaymentProviderClient> clients) {
        this.repository = repository;
        this.clientMap = clients.stream()
                .collect(Collectors.toMap(PaymentProviderClient::getProviderCode, c -> c));
    }

    @Transactional(readOnly = true)
    public List<ProviderResponseDto> getAllProviders() {
        return repository.findAll().stream()
                .map(ProviderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentProvider> getActiveProviders() {
        return repository.findByStatus(ProviderStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public PaymentProvider getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public PaymentProvider getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with code: " + code));
    }

    public PaymentProviderClient getClient(String providerCode) {
        PaymentProviderClient client = clientMap.get(providerCode);
        if (client == null) {
            throw new ResourceNotFoundException("No provider client implementation registered for code: " + providerCode);
        }
        return client;
    }

    @Transactional
    public ProviderResponseDto updateConfig(UUID providerId, UpdateProviderConfigRequest request) {
        PaymentProvider provider = repository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + providerId));

        if (request.getSuccessRate() != null) {
            provider.setSuccessRate(request.getSuccessRate());
        }
        if (request.getAvgLatencyMs() != null) {
            provider.setAvgLatencyMs(request.getAvgLatencyMs());
        }
        if (request.getTimeoutRate() != null) {
            provider.setTimeoutRate(request.getTimeoutRate());
        }
        if (request.getFailureRate() != null) {
            provider.setFailureRate(request.getFailureRate());
        }
        if (request.getPriorityWeight() != null) {
            provider.setPriorityWeight(request.getPriorityWeight());
        }
        if (request.getStatus() != null) {
            provider.setStatus(request.getStatus());
        }

        PaymentProvider saved = repository.save(provider);
        return ProviderResponseDto.from(saved);
    }
}
