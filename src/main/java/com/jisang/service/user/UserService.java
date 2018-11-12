package com.jisang.service.user;

import com.jisang.dto.user.AuthUserResponseDTO;
import com.jisang.dto.user.UserModificationDTO;
import com.jisang.dto.user.SignupDTO;

/**
 * 
 * {@link User{ 도메인 관련 비즈니스 로직을 정의한 서비스 인터페이스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface UserService {
	public AuthUserResponseDTO findUserForManagement(int uid);
	// public UserResponseDTO findUser(int uid);
	public void registerUser(SignupDTO dto);
	public UserModificationDTO modifyUser(UserModificationDTO user);
}
