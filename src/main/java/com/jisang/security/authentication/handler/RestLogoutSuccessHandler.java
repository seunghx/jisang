package com.jisang.security.authentication.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.exception.JWTAuthenticationException;
import com.jisang.security.persistence.RedisUserDAO;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;



/**
 * 
 * jisang 어플리케이션의 기본 logout successhandler 구현.
 * 
 * @author leeseunghyun
 *
 */
@Component
public class RestLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler{
	

	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private RedisUserDAO userDAO;
	@Autowired
	private JWTServiceResolver jwtServiceResolver;
	
	
	// Constructors
	//==========================================================================================================================


	// Methods
	//==========================================================================================================================

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		logger.info("Starting logout process.");
		
		String tokenHeader = request.getHeader(JWTService.JWT_HEADER_NAME);
		
		if(StringUtils.isEmpty(tokenHeader)) {
			throw new AuthenticationCredentialsNotFoundException("JWT token is not present");
		}
		
		if(!tokenHeader.startsWith(JWTService.JWT_PREFIX)) {
			logger.warn("Illegal acceess with unknown token received. : {} ", tokenHeader);
			logger.warn("this token does not start with {}", JWTService.JWT_PREFIX);
			
			throw new JWTAuthenticationException("Received unknown token : " + tokenHeader);
		}
		
		UserAuthTokenDTO userTokenDTO = (UserAuthTokenDTO)jwtServiceResolver.resolveJWTService(UserAuthTokenDTO.class)
						  						.parseToken(tokenHeader.substring(JWTService.JWT_PREFIX.length(), tokenHeader.length()));
		
		userDAO.delete(userTokenDTO.getAccount().getId());
		
		response.setStatus(HttpStatus.CREATED.value());
		
		logger.info("Logout succeeded.", userTokenDTO.getAccount());
	}
}
