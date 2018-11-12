package com.jisang.security.dto;

import javax.validation.constraints.NotBlank;

import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.JWTParsing;

public class AnonymousUserAuthTokenDTO implements TokenDTO {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    @NotBlank(groups = { JWTParsing.class })
    private String token;

    @NotBlank(groups = { JWTParsing.class })
    private String clientIPAddr;

    @NotBlank(groups = { JWTBuilding.class })
    private String userEmail;

    private boolean authenticated;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    @Override
    public String getToken() {
        return token;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getClientIPAddr() {
        return clientIPAddr;
    }

    public void setClientIPAddr(String clientIPAddr) {
        this.clientIPAddr = clientIPAddr;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

}
