package com.jisang.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.jisang.support.validation.Korea;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 회원 등록에 사용 될 정보를 담는 DTO 클래스.
 * 
 * @author leeseunghyun
 *
 */
public class SignupDTO {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================
    @ApiModelProperty(notes = "유저 이메일.", name = "email", required = true)
    @NotBlank(message = "메일을 입력해 주세요.")
    @Email(message = "올바른 형식의 이메일이 아닙니다.")
    private String email;

    @ApiModelProperty(notes = "유저 비밀번호", name = "password", required = true, example = "password")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @ApiModelProperty(notes = "유저 핸드폰 번호", name = "phone", required = true, example = "01012341234", value = "하이픈(-)을 제외한 10-11자리의 숫자로만 구성 되어야함.")
    @NotBlank(message = "핸드폰 번호를 입력해주세요.")
    @Pattern(regexp = "^\\+821([0|1|6|7|8|9])\\-([0-9]{3,4})\\-([0-9]{4})$", message = "하이픈('-')을 제외한 10~11자리의 올바른 핸드폰 번호를 입력하세요.", groups = {
            Korea.class })
    private String phone;

    @ApiModelProperty(notes = "유저 이름", name = "name", required = true)
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    /**
     * 해당 값은 일반유저 관리자 유저 각각에 대한 핸들러 메서드에서 설정한다.
     */
    private String role;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[email=" + email + ", password=" + password + ", name=" + name + ", phone="
                + phone + ", role=" + role + "]";
    }

}
