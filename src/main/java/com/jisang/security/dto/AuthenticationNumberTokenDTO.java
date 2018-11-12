package com.jisang.security.dto;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.JWTParsing;

public class AuthenticationNumberTokenDTO implements TokenDTO {

    // Instance Fields
    // ==========================================================================================================================

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @NotBlank(groups = { JWTBuilding.class, JWTParsing.class })
    private String clientIPAddr;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @NotBlank(groups = { JWTBuilding.class, JWTParsing.class })
    private String authenticationNumber;

    @JsonIgnore
    @NotBlank(groups = { JWTParsing.class })
    private String token;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @NotBlank(groups = { JWTBuilding.class })
    private String userEmail;

    // Methods
    // ==========================================================================================================================

    public String getUserEmail() {
        return userEmail;
    }

    public String getClientIPAddr() {
        return clientIPAddr;
    }

    public String getAuthenticationNumber() {
        return authenticationNumber;
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setClientIPAddr(String clientIPAddr) {
        this.clientIPAddr = clientIPAddr;
    }

    public void setAuthenticationNumber(String authenticationNumber) {
        this.authenticationNumber = authenticationNumber;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[clientIPAddr=" + clientIPAddr 
                                    + ", authenticationNumber=" + authenticationNumber 
                                    + ", token=" + token + ", userEmail=" + userEmail + "]";
    }

}
