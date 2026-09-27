package com.payroute.platform.provider.controller;

import com.payroute.platform.common.api.ApiResponse;
import com.payroute.platform.provider.dto.ProviderResponseDto;
import com.payroute.platform.provider.dto.UpdateProviderConfigRequest;
import com.payroute.platform.provider.service.ProviderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Payment Providers", description = "Provider configuration, status monitoring, and simulation controls")
public class ProviderController {

    private final ProviderConfigService providerConfigService;

    public ProviderController(ProviderConfigService providerConfigService) {
        this.providerConfigService = providerConfigService;
    }

    @GetMapping
    @Operation(summary = "List all payment providers and their simulated configurations")
    public ResponseEntity<ApiResponse<List<ProviderResponseDto>>> listProviders() {
        List<ProviderResponseDto> providers = providerConfigService.getAllProviders();
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve specific payment provider configuration")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> getProvider(@PathVariable UUID id) {
        ProviderResponseDto provider = ProviderResponseDto.from(providerConfigService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(provider));
    }

    @PatchMapping("/{id}/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @Operation(summary = "Dynamically update provider simulation parameters (latency, timeout %, failure %)")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> updateConfig(
            @PathVariable UUID id,
            @RequestBody UpdateProviderConfigRequest request) {
        ProviderResponseDto updated = providerConfigService.updateConfig(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
