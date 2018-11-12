package com.jisang.service.user;

import java.util.Objects;

import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import com.jisang.domain.User;
import com.jisang.dto.user.AuthUserResponseDTO;
import com.jisang.dto.user.UserModificationDTO;
import com.jisang.dto.user.SignupDTO;
import com.jisang.dto.user.SignupManagerDTO;
import com.jisang.domain.Address;
import com.jisang.domain.Market;
import com.jisang.persistence.MapDAO;
import com.jisang.persistence.ManagementDAO;
import com.jisang.persistence.MarketDAO;
import com.jisang.persistence.UserDAO;
import com.jisang.support.AddressAleadyUsedException;
import com.jisang.support.NoSuchAddressException;

import static com.jisang.config.code.CodeBook.UserType.MANAGER_USER;
import static com.jisang.config.code.CodeBook.UserType.NORMAL_USER;





/**
 * 
 * {@link User} 도메인 관련 비즈니스 로직을 수행하는 서비스 오브젝트이다. 
 * 
 * @author leeseunghyun
 *
 */
@Service
public class UserServiceImpl implements UserService{
	
	
	// Static Fields
	//==========================================================================================================================
	
	
	// Instance Fields
	//==========================================================================================================================

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Autowired 
	private ModelMapper modelMapper;
	@Autowired
	private PlatformTransactionManager txManager;
	@Autowired @Lazy
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private ManagementDAO manageDAO;
	@Autowired
	private MarketDAO marketDAO;
	@Autowired
	private MapDAO mapDAO;

	
	// Constructors
	//==========================================================================================================================
	

	// Methods
	//==========================================================================================================================

	/**
	 * 
	 * 유저 아이디로부터 유저 정보를 찾아 {@link AuthUserResponseDTO} 오브젝트를 담는다. 
	 * 
	 * 현재 지상 어플리케이션 워크 플로우 상에는 존재하지 않지만 회원 정보 보기 및 수정은 기본적으로 제공되어야 한다고 생각하여 (또한 간단한 만큼)회원 정보를 반환하는 
	 * 비즈니스 로직을 추가하려고 하였다. 생각을 해보니 다른 회원이 보는 회원 정보와 자신이 (수정 등을 목적으로)보게 되는 회원 정보가 다를 필요가 있었다. 그래서 
	 * 자신의 회원 정보를 반환하는 목적으로 {@link AuthUserResponseDTO} 오브젝트를 정의하게 되었다. 구현하지는 않았지만 다른 회원에게 보여줄 회원 정보는 
	 * {@link UserResponseDTO}라고 할 예정이다. 같은 이유로 핸들러 메서드를 둘로 나누기로 하였다. 자신의 정보를 보려면 인증이 필요하므로 첫 번째 핸들러 
	 * 메서드는 /auth/user 라는 url로 접근되며 다른 회원의 정보를 보는 핸들러 메서드는 /user 와 같은 url로 접근될 수 있을 것이다. (현재는 자신의 회원 정보만
	 * 조회하는 비즈니스 로직만 구현하였음.) 여기서 고민이 생겻다. 유저 관련 비즈니스 로직을 담는 이 클래스 {@link UserServiceImpl}에서 도메인 오브젝트
	 * {@link User}를 반환하는 메서드(예를 들어 {@code findUser()}) 하나를 정의하고 각각의 두 경우의 핸들러 메서드에서 이 메서드({@code findUser()})
	 * 를 호출한 후 자신의 응답을 위해 필요한 작업을 수행(도메인 오브젝트를 DTO로 변환 혹은 도메인 오브젝트로부터 데이터 가공)하느냐 아니면 아래 구현과 같이 
	 * 각각의 핸들러 메서드에서 호출 할 서비스 메서드를 정의하여 이 메서드들에서 {@link AuthUserResponseDTO, UserResponseDTO}를 반환하게 하여 컨트롤러에
	 * 비즈니스 로직을 줄이느냐이다. 결국 난 후자의 방법을 선택하였다. 
	 * 
	 * 클라이언트로 응답되는 DTO 오브젝트에는 도메인 오브젝트에는 존재하지 않는 프로퍼티 등이 필요하게 될 수도 있다. 이런 데이터들은 도메인 오브젝트를 가공하거나 
	 * 외부의 정보로 부터 가져오거나 하게 될 것이다. 만약 전자의 방법이었다면 이런 상황에서 DTO 구성 로직까지 컨트롤러가 수행하다보니 컨트롤러의 비즈니스 로직 코드가 
	 * 늘어나며 반대로 서비스 오브젝트는 전혀 하는일이 없이 단지 DAO가 반환하는 도메인 오브젝트만 그대로 전달하게 된다. 가급적 비즈니스 로직은 서비스 계층에 담기로
	 * 정하였기 때문에 아래와 같이 서비스 계층 메서드에서 {@link AuthUserResponseDTO}와 같은 구체적 DTO 오브젝트를 반환하는 방법을 선택하였다.
	 * 
	 * 유저 정보 반환이라는 점에서는 같은 비즈니스 로직이라고도 볼 수 있으나 정보와 정보를 받는 대상이 다르기 때문에 유저 자신의 정보 보기, 다른 유저의 정보 보기는
	 * 다른 비즈니스 로직이라고 봐야하며 그렇기 때문에 아래와 같이 메서드를 나누는 것({@code findUser()}를 구현하지 않았음)이 더 옳다고 생각한다.
	 * 
	 */
	@Override
	public AuthUserResponseDTO findUserForManagement(int userId) {
		User user = userDAO.read(userId);
		
		logger.debug("Finding user info with user id : {} succeeded. Converting User Domain object to DTO", userId);
		
		return modelMapper.map(user, AuthUserResponseDTO.class);
	}
	
	/**
	 * 
	 * 전달 받은 유저 정보로부터 일반 유저인지 매니저인지를 구분 한 후 각각에 해당하는 private 유저 등록 메서드를 호출한다.
	 * 
	 */
	@Override
	public void registerUser(SignupDTO signupDTO) {
		
		Objects.requireNonNull(signupDTO, "Null value signupDTO object detected while trying to signup user.");		
		
		if(NORMAL_USER.equalsByCode(signupDTO.getRole())) {
			signupDTO.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
			registerNormalUser(signupDTO);
		}else if(MANAGER_USER.equalsByCode(signupDTO.getRole())) {
			if(!(signupDTO instanceof SignupManagerDTO)) {
				logger.warn("Invalid user role detected. Received user role : {} but signupDTO : {}", signupDTO.getRole(), signupDTO.getClass());
				logger.warn("This problem mitght be caused by contoller code. Fix it.!!.");
				throw new IllegalStateException("Invalid mapping between user role property & DTO object.");
			}
			signupDTO.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
			registerManager((SignupManagerDTO)signupDTO);
		}else {
			logger.warn("Invalid user role detected!. user role : {}", signupDTO.getRole());
			logger.warn("This problem might be caused by contoller code. Fix it!!.");
			throw new IllegalStateException("Unsupported user role detected!.");
		}
	}
	
	/**
	 * 일반 유저에 대한 등록 메서드이다. {@code userDAO}의 메서드를 호출해 데이터 엑세스 작업을 위임한다.
	 */
	private void registerNormalUser(SignupDTO dto) {
		logger.debug("Registering normal user...");
		
		userDAO.create(modelMapper.map(dto, User.class));
		
		logger.debug("Normal user registration succeeded.");
	}
	
	/**
	 * 
	 * 매니저 유저에 대한 등록 메서드이다. {@code userDAO, marketDAO, manageDAO, s3DAO} 등이 사용됨을 알 수 있는데 {@code manageDAO}가 사용되는
	 * 이유는 {@link ManagementDAO}의 주석에 설명하였다.
	 * 
	 */
	private void registerManager(SignupManagerDTO dto) {
		logger.debug("Registering manager...");
		
		User user = new User();
		Market market = new Market();
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		def.setReadOnly(false);

		TransactionStatus status = txManager.getTransaction(def);
				
		try {
			registerAddress(dto.getMarketAddress(), dto.getMarketLocation());
			
			modelMapper.map(dto, user);		
			userDAO.create(user);
			
			modelMapper.map(dto, market);
			marketDAO.create(market);
			
			mapDAO.updateAddressMarketId(dto.getMarketAddress(), dto.getMarketLocation(), market.getId());
			
			manageDAO.create(user.getId(), market.getId());
			txManager.commit(status);
		}catch(DataAccessException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Processing current transaction rollback. Caused by {}", e.toString());
			}
			txManager.rollback(status);
			throw e;
		}catch(MappingException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Processing current transaction rollback. Caused by {}", e.toString());
			}
			txManager.rollback(status);
			throw e;
		}catch(RuntimeException e) {
			logger.warn("Unexpected exception occurred. exception : {}", e.toString());
			logger.info("Processing current transaction rollback.");
			
			txManager.rollback(status);
			throw e;
		}
		
		logger.debug("Manager registration succeeded.");
		
	}
	
	private void registerAddress(String addressId, String location) {
		
		/*
		 * 아래 if절을 통한 검사 부분을 컨트롤러 메서드 파라미터 바인딩 시에 DAO에 접근하여 검사하게 하는 방법도 있겠다.
		 * 무엇이 더 좋은 방법인지 모르겠는데 그 이유는 컨트롤러 메서드 파라미터 바인딩에 성공한 정보가 
		 * 컨트롤러나 그 외 다른 오브젝트를 거치는 과정 중 변경될 수도 있기 때문이다. 과한 걱정인가 싶기도 하다.
		 * 오동작하는 클래스가 어플리케이션에 최종적으로 추가되지는 않을 것이기 때문이다.
		 */
		Address address = mapDAO.readAddressByAddressIdAndLocation(addressId, location);

		if(address == null) {
			logger.info("Received non-existing address id.");
			throw new NoSuchAddressException("Received address information does not exist.", addressId, location);
		}else if(address.getMarket() != null){
			logger.info("Received address is aleady used by other market.");
			throw new AddressAleadyUsedException("Received address is aleady used ", addressId, location);
		}
	}

	/**
	 * 
	 * 유저 정보를 수정한다. 현재의 구현으로는 도메인 오브젝트 {@link User}를 생성하여 DAO에 전달하는 것 밖에는 없다.
	 * update를 하고 {@link UserModificationDTO} 오브젝트를 다시 반환하는 이유는 마켓 정보 수정 화면에서 수정 버튼이 눌려지면
	 * 수정 된 정보를 보여줘야 하기 때문이다. 물론 클라이언트 구현에 따라 수정 후 다시 GET 요청을 하는 방법이 있겠으나 이 방법이 더 편할 것이라고 생각하였다.
	 * 
	 * 이 메서드는 업데이트만 하게 하고 이 메서드를 호출하는 handler 메서드에서 클라이언트로부터 전달 받은 DTO를 다시 응답하는 방법도 있겠다. 
	 * 현재 구현으로는 이 방법이나 아래 메서드 구현과 같은 방법이나 다를바는 없으나 후에 요구 사항/비즈니스 로직이 추가될 경우 이 메서드에서 
	 * {@link UserModificationDTO}의 정보를 변경할 수도 있는 것이기 때문에 이 메서드에서 반환하는게 났다고 생각하였다. 가급적 비즈니스 로직은 컨트롤러보단
	 * 서비스 계층에 두려고 한다.
	 * 
	 * 이 클래스의 다른 메서드와 마찬가지로 로깅은 {@link UserServiceAspect}에 의해 수행된다.
	 * 
	 */
	@Override
	public UserModificationDTO modifyUser(UserModificationDTO dto) {
		
		Objects.requireNonNull(dto, "Null value argument dto detected while trying to modify user ifno.");
		
		User user = modelMapper.map(dto, User.class);
		
		if(!StringUtils.isEmpty(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		
		userDAO.update(user);
		
		return dto;
	}
}
