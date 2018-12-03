package com.jisang.security.service;

import org.springframework.security.authentication.AuthenticationProvider;

import com.jisang.security.dto.TokenDTO;

/**
 * 
 * 지상 어플리케이션의 유저 인증에 사용될 JWT 토큰의 building{@link #buildToken(TokenDTO)},
 * parsing{@link #parseToken(JWT)}을 담당한다.
 * 
 * 사실 처음에는 이 인터페이스를 정의하지 않았었다. JWT building, parsing 메서드를 public 스코프로 두기 싫었기
 * 때문인데 결국 아래와 같은 이유로 이 인터페이스를 정의하게 되었다.
 * 
 * 이 인터페이스를 정의하지 않았던 처음의 나의 생각은, 다른 패키지에 이 인터페이스 구현의 메서드가 노출될 경우 잘못된 JWT token이
 * 생성되어 버릴 지도 모른다는 의심 때문이었다. ({@link io.jsonwebtoken}와 같은 라이브러리를 직접 이용하지 않는다는
 * 선에서) JWT token이 잘못 생성된다는 것은 어플리케이션의 보안에 큰 해가 될 수 있다.
 * 
 * 그래서 {@code Authentication Filter}와 {@link AuthenticationProvider} 구현은 어플리케이션의
 * 인증에 중요한 역할을 하는 컴포넌트인 만큼 이들 클래스는 신뢰할 수 있다는 가정하에, JWT token의 안전한 생성을 위하여
 * {@code Authentication Filer}(Servlet Filter) 및 {@link AuthenticationProvider}
 * 구현과 같은 패키지 {@code com.jisang.security.authentication}에 이 클래스를 정의하기로 하였었다.
 * 
 * 이 인터페이스 구현 클래스의 메서드가 {@code com.jisang.security.authentication} 패키지 외부에서 호출
 * 가능할 경우, 전달을 목적으로 {@link TokenDTO}를 내부 프로퍼티로 담고있는 {@link Authentication} 구현 및
 * {@link UserDetails} 구현 등 다른 패키지의 컴포넌트에서 이 인터페이스 구현의 메서드 {@link buildToken} 등을
 * 호출하여 유효하지 않은 {@link TokenDTO}를 반환할 수도 있다고 생각하였다. 사실 그래서 처음에는
 * {@link TokenDTO}도 이 클래스와 같은 패키지에 두고 setter 메서드의 스코프를 디폴트스코프(패키지-프라이빗)으로
 * 정의하였었다. ({@code com.jisagn.security.authentication} 패키지의 클래스들은 신뢰할 수 있다고 가정.)
 * 
 * 그러나 이런 방법만으로는 어차피 안전한 JWT token의 생성을 보장할 수 없다. 외부 컴포넌트에서 정말 악의가 있다면 리플렉션이라도
 * 이용할 것이기 때문이다. 또한 이 인터페이스의 구현을 거치지 않고 바로 {@link io.jsonwebtoken}과 같은 JWT 관련
 * 라이브러리를 직접 이용할 수도 있다.
 * 
 * 또 다른 문제도 있는데 웹 응답 헤더에 담긴 JWT token을 변경 불가능하게 할 수가 없기 때문에 JWT token이 안전하게
 * 생성되었다고 하더라도 결과적으로 완벽하게 JWT token이 신뢰 가능하다고 장담할 수 없다. 다른 컴포넌트에서 HTTP 응답 헤더
 * Authorization 필드를 수정해버릴 수도 있기 때문이다. {@link HttpServletResponseWrapper}를 구현해
 * {@link HttpServletResponse#setHeader}등의 메서드를 오버라이드하여 수정을 못하게 막을 수도 있겠지만 또 다른
 * {@link HttpServletResponseWrapper}를 구현해 메서드를 다시 오버라이딩할 경우 이 마저도 무용지물이 된다. 결국
 * 어플리케이션 전체에서의 JWT token의 신뢰성을 보장할 수 없다. 또한 위와 같은 의심이 합리적이라면 같은 패키지의 내의 다른
 * 컴포넌트도 신뢰할 수 없을 것이다.
 * 
 * 
 * 앞으로는 이런 (잘 만들어보려는 생각에 의한)과한 의심은 하지 말아야겠다는 교훈을 얻었다. 안정적인 프로그램을 만들려는 시도가 목적을
 * 달성하지 못하면 프로그램의 제약만 늘어나버릴 지도 모른다. 실제 배포되는 컴포넌트는 충분한 테스트와 검사를 거쳤을 것이기 때문에
 * 어플리케이션 내의 다른 컴포넌트가 잘못된 동작을 취할지도 모른다는 의심은 과하면 좋지 않은 것 같다. 앞으로는 이렇게 하지 말아야겠다.
 * 다른 클래스 또는 컴포넌트의 동작에 대한 과한 의심은 버리고 외부로부터 전달 받은 오브젝트에 대한 유효성 검사 정도만 수행 해야겠다.
 * 
 * 위와 같은 교훈으로부터 과도한 의심을 버리고 이 인터페이스를 정의하기로 하였다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface JWTService {

    // Static Fields
    // ==========================================================================================================================

    public static final String JWT_PREFIX = "Bearer :";
    public static final String JWT_HEADER_NAME = "Authorization";

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * JWT token 구성 정보를 담고 있는 {@code TokenDTO} 오브젝트를 바탕으로 JWT token 문자열을 생성한다.
     * 
     */
    public String buildToken(TokenDTO tokenDTO);

    /**
     * 
     * JWT token 파싱을 하는 이 메서드에서 인자로 {@link TokenDTO}를 받고 있는데, 단순히 JWT token 문자열만을 받지
     * 않은 이유는 parsing을 수행하는 과정 중 처리되는 검증 작업에 필요한 추가 정보가 필요할 수 있기 때문이다.
     * (IP address 와 같은)
     * 
     */
    public TokenDTO parseToken(TokenDTO tokenDTO);

    /**
     * 전달받은 JWT token 문자열을 파싱한다. 토큰에 대한 검증 작업을 거칠 필요없을 경우 이 메서드를 사용할 수 있다.
     */
    public TokenDTO parseToken(String jwtToken);

    /**
     * 전달 받은 인자 {@code tokenDTO}를 이 클래스의 구현이 지원 가능한지 아닌지를 반환한다. 이 메서드는
     * {@link JWTServiceResolver}에서 요청에 알맞은 이 클래스의 구현(추상 클래스 계승)을 선택하기 위해 사용된다.
     */
    public boolean supports(Class<? extends TokenDTO> tokenDTO);
}
