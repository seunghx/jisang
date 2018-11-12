package com.jisang.security.core.userdetails;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jisang.security.domain.Account;
import com.jisang.security.persistence.SecurityUserDAO;
import com.jisang.security.validation.SecurityDataAccess;
import com.jisang.security.validation.SecurityValidationDelegator;


/**
 *
 * @author leeseunghyun
 */
@Service("default")
public class DefaultUserDetailsService implements UserDetailsService { 
	
	
	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private SecurityUserDAO userDAO;
	@Autowired
	private SecurityValidationDelegator validator;
	

	// Constructors
	//==========================================================================================================================


	// Methods
	//==========================================================================================================================

	/**
	 *  DAO로부터 전달 받은 {@link Account} 오브젝트를 담은 {@link UserDetails} 구현 오브젝트를 반환한다.
	 * 
	 *  인증 필터 등에서 파라미터 {@code username}에 대한 null 검사를 수행했음을 알지만 안전을 위해 null 검사를 수행한다.
	 * 
	 *  @throws AuthenticationServiceException  데이터베이스 시스템에서 예외가 발생할 경우 이 예외가 던져진다.
	 *  @throws UsernameNotFoundException 전달받은 {@code username}에 해당하는 유저 정보가 없을 경우 이 예외가 던져진다. 
     *  @throws InternalAuthenticationServiceException {@link SecurityValidationDelegator#validate}
     *  
	 */
	@Override
	public DefaultUserDetails loadUserByUsername(String username) {
		
		logger.info("Starting to load user info.");
		
		if(StringUtils.isEmpty(username)) {
			Objects.requireNonNull(username, "Username used to retrieve user password is null");
		}
		
		Account account = Optional.ofNullable(userDAO.find(username))
								  .orElseThrow(
										()-> new UsernameNotFoundException("Username does not exist. username : " + username)
								  );
			
		validator.validate(account, SecurityDataAccess.class);		
			
		logger.info("Loading user info succeeded.");
			
		return new DefaultUserDetails(account);

	}	
}
