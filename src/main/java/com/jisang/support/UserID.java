package com.jisang.support;

import springfox.documentation.spring.web.plugins.Docket;


/**
 * 
 * 유저의 id를 담은 클래스로 {@link Docket#ignoredParameterTypes(Class...)} 설정 및 {@link UserIDArgumentResolver} 
 * 적용을 위해 정의하게 된 핸들러 메서드 파라미터로 사용될 클래스이다. 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class UserID {
	
	private int userId;

	public UserID(int userId) {
		this.userId = userId;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[userId=" + userId + "]";
	}
}
