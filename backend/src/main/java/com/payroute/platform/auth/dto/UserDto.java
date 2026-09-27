package com.payroute.platform.auth.dto;

import com.payroute.platform.auth.entity.Role;
import java.util.UUID;

public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private Role role;
    private UUID merchantId;

    public UserDto() {
    }

    public UserDto(UUID id, String username, String email, Role role, UUID merchantId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.merchantId = merchantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }
}
