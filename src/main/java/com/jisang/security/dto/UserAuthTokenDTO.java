package com.jisang.security.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.JWTParsing;
import com.jisang.security.validation.UserAuthentication;
import com.jisang.security.domain.TokenComponent;
import com.jisang.security.domain.Account;
import com.jisang.security.persistence.RedisUserDAO;
import com.jisang.security.service.UserAuthJWTService;
import com.jisang.security.service.JWTService;


/**/
/**
 * <p>
 * 지상 어플리케이션의 기본 유저 인증(로그인 인증, 로그인 유저의 상태 저장을 위한 JWT token 인증) 과정에 {@code Authentication Filter}와 
 * {@link AuthenticationProvider} 구현, {@link JWTService} 구현 등에서 사용될 DTO 클래스이다. <br><br>
 * 
 * 초기에는 user의 email, role, password를 담은 {@link Account} 오브젝트와 JTI 정보를 모두 RDB로부터 가져와 이 클래스에 담았었는데, 
 * {@link RedisUserDAO}에서 설명하였듯, JTI는 redis로부터 가져오기로 변경하게 되었다. 이로부터 기존 유저 인증 과정에 참여하는 여러 클래스와 마찬가지로
 * 위의 정보들을 담고 있던 이 클래스에도 변경이 필요하게 되었다. <br><br>
 * 
 * JTI를 redis로부터 가져오기로 결정하고 나서 새로 정의한 {@link TokenComponent} 클래스는 현재는 {@code email} 프로퍼티와 {@code jti} 프로퍼티를 담고 있다.
 * 현재로서는 추가할 만한 정보가 있을지 잘 모르겠으나 후에 인증 과정(로그인 인증이 아닌 로그인 유지를 위한 JWT token 인증)에 DB로부터 새로 가져와야할 정보가 생길 경우 
 * 이 클래스에 새로운 프로퍼티를 추가할 계획이다. 또한 이렇게 정보가 추가될 것을 고려하여 이 클래스는 redis에 Hash 타입으로 저장되도록 설계하였다.(Hash 타입으로 결정한
 * 자세한 내용은 {@link TokenComponent}의 주석에 설명하였다.) 이 때문에 {@link TokenComponent}를 단순 문자열 타입이 아닌 클래스로 정의하게 된 것이다.<br><br>
 * 
 * RDB로부터 JTI를 조회하는 시나리오 상에서 작성된 아래의 클래스({@link UserAuthTokenDTO})에도 {@code jti} 프로퍼티가 존재했다. 이 클래스도 
 * {@link TokenComponent}와 마찬가지로 후에 DB로부터 또 다른 인증 정보(JTI와 같은 유저 외적인 정보 - 엄밀히 말하면 JTI는 유저라는 오브젝트를 구성하는 정보라고 
 * 할 수는 없다고 생각한다. 유저를 구성하는 정보는 유저의 이름, 아이디, 핸드폰 번호 등이 더 알맞겠다.)를 가져올 일이 생기면 프로퍼티를 새로 추가할 예정이었다. 
 * 그러나 상황이 변하였기 때문에(redis 사용) 이제 인증 정보가 새로 추가될 경우 이 클래스에 새 프로퍼티가 추가될 것이 아니라 {@link TokenComponent}의 프로퍼티가 
 * 추가되어야 한다. 그러므로 이 클래스에는 기존 {@code jti}와 같은 프로퍼티는 더 이상 없고 {@link TokenComponent} 타입 프로퍼티가 생겨야 한다. 위에 말하였지만  
 * 그러나 일단 JTI 외에 새로 추가 될 정보가 생길 때까지는 그렇게 하지 않기로 했다.<br><br>
 * 
 * 
 * 이 클래스에서 {@link Account}와 {@link TokenComponent}를 모두 두어야 하는 이유는 {@link UserAuthJWTService}에서 JWT token building, 
 * parsing 시에 이 두 클래스가 담고 있는 정보가 모두 필요하기 때문이다. 이 두 클래스는 RDB로 부터 가져오는 유저 구성 정보냐 redis로부터 가져오는 유저에 대한 토큰
 * 구성 정보냐에 차이 때문에 분리되어 있을 뿐이다. <br><br>
 * 
 * 
 * 차라리 {@link TokenComponent}가 {@link TokenDTO} 인터페이스를 구현하게 하고 {@link Account} 타입 필드를 두게 한다음 이 클래스 
 * {@link UserAuthTokenDTO}를 없애는 것도 좋은 방법일 수 있다. redis로부터 받아 온 {@link TokenComponent} 오브젝트에 RDB로부터 받아 온 {@link Account}
 * 오브젝트를 내부 필드에 set만 해주면 되기 때문이다. 그러나 그럴 경우 JWT claim으로의 serialization과 redis로의 serialization을 {@link TokenComponent} 
 * 한 클래스에 대하여 수행하게 되므로 직렬화 과정에 원치 않는 프로퍼티가 직렬화 되는 것을 방지할 방법이 필요하다. 이런 방법으로는 예를 들어 {@link @JsonFilter} 애노테이션 
 * 사용 등의 방법이 있겠으나 이 방법은 이 방법대로 복잡해지고 변경요소가 늘어나는 것 같다.<br><br>
 * 
 *
 * 초기의 구현에 비해 많이 복잡해진 것 같긴하다.
 *</p> 
 *  
 * @author leeseunghyun
 *
 */

public class UserAuthTokenDTO implements TokenDTO {	
	
	
	// Instance Fields from database(RDB, Redis ...)
	//==========================================================================================================================

	@Valid
	@NotNull(groups = {UserAuthentication.class, JWTBuilding.class})
	private TokenComponent tokenComponent = new TokenComponent();
	@Valid
	@NotNull(groups = {UserAuthentication.class, JWTBuilding.class})
	private Account account = new Account();
		
	
	// Instance Fields from HttpServletRequest
	//==========================================================================================================================

	/**
	 * JWT token parsing을 위해 {@link JWTService#parseToken(TokenDTO} 메서드에서 사용 될 JWT token 문자열.
	 */
	@NotBlank(groups = {JWTParsing.class})
	private String token;
	
	
	// Constructors
	//==========================================================================================================================

	public UserAuthTokenDTO() {}
	
	public UserAuthTokenDTO(Account account, TokenComponent tokenComponent) {
		this.account = account;
		this.tokenComponent = tokenComponent;
	}
	
	// Methods
	//==========================================================================================================================

	@Override
	public String getToken() {
		return token;
	}
	

	public TokenComponent getTokenComponent() {
		return tokenComponent;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setToken(String token) {
		this.token = token;
	}
	

	public void setTokenComponent(TokenComponent tokenComponent) {
		this.tokenComponent = tokenComponent;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[account=" + account + ", tokenComponent=" + tokenComponent + ", token=" + token +  "]";
	}
	
/*
	/**
	 * 
	 * <span style="text-decoration:line-through;" data-description="초기의 redis 사용 이전 버전에서의 주석. (초기에는 RDB만을 사용하였다.)">
	 * 
	 * 데이터베이스 상에는 {@code JTI}를 나타내는 칼럼({@code user_jti})이 다른 user 정보와 함께 테이블 tbl_users 내에 있다. 이와 달리 이 클래스의 프로퍼티를
	 * 보면 계정 정보를 나타내는 {@code user} 프로퍼티 외부에 또 다른 프로퍼티로서 {@code jti}가 존재하는 것을 알 수 있다. {@code jti} 프로퍼티가
	 * {@code user}안에 있어야할 것으로 생각될 수 있으나 데이터베이스 테이블을 정규화시키지 않아 {@code user_jti} 칼럼이 테이블 tbl_users 내에 있는 것일 뿐이다. 
	 * 데이터베이스 테이블과 어플리케이션 객체의 구조가 일관적이지 않긴 하지만 자주 조회되는 칼럼에 대하여 매번 조인 연산을 수행하는 것보다는 낫다고 판단하였다. 
	 * 
	 * </span>
	 * 
	 
	@NotBlank(groups = {UserAuthentication.class, JWTBuilding.class})
	private String jti;
	*/
}
