package com.jisang.security.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.jisang.support.validation.Korea;

public class PhoneAuthenticationDTO {

    // Instance Fields
    // ==========================================================================================================================

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^01([0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$", groups = { Korea.class })
    private String phone;

    // Methods 
    // ==========================================================================================================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[email=" + email + ", phone=" + phone + "]";
    }

}
