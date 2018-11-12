package com.jisang.security.domain;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.SecurityDataAccess;
import com.jisang.security.validation.UserAuthentication;



/**/
/**
 * 
 * 지상 어플리케이션의 security 단에서 인증에 사용될 유저의 계정 정보를 담은 클래스이다. 비록 유저의 {@code id}, {@code role}, {@code password}
 * 정보 밖에 없지만 seucirty 단 입장에서는 도메인이라고 볼 수 있다는 생각에 {@code com.jisang.security.domain} 패키지에 두었다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class Account {

	
	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================
	
	@NotNull(groups = {UserAuthentication.class, JWTBuilding.class})
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Integer id;
	
	@NotBlank(groups = {UserAuthentication.class, JWTBuilding.class})
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String role;
	
	@NotBlank(groups = {SecurityDataAccess.class})
	@JsonIgnore
	private String password;
	
	
	// Constructors
	//==========================================================================================================================

	
	// Methods
	//==========================================================================================================================

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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

	@Override
	public String toString() {
		return this.getClass().getName() + "[id=" + id + ", password=" + password + ", role=" + role + "]";
	}

}
