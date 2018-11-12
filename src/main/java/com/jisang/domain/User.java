package com.jisang.domain;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * 유저 계정 정보를 나타내는 domain 클래스.
 * 
 * @author leeseunghyun
 *
 */
public class User {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    /** mybatis의 옵션 속성 useGeneratedKey에 의해 설정될 auto_increment 값. */
    @JsonIgnore
    private Integer id;
    @NotBlank
    private String email;
    @NotBlank
    @JsonIgnore
    private String password;
    @NotBlank
    private String role;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[email=" + email + ", password=" + password + ", role=" + role 
                                         + ", name=" + name + ", phone=" + phone + "]";
    }

}
