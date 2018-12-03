package com.jisang.security.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import com.jisang.security.domain.Account;
import com.jisang.security.domain.TokenComponent;
import com.jisang.security.dto.TokenDTO;
import com.jisang.security.dto.UserAuthTokenDTO;

/**/
/**
 * 
 * 기본 적인 유저 인증 작업(로그인 인증 성공시 token building, JWT token 인증시 token parsing)에 사용되는
 * {@link JWTService} 구현.
 * 
 * <p>
 * 
 * 일반적인 JWT token 기반 인증 시스템에는 두 종류의 token이 존재한다. 하나는 access-token으로
 * 자원에 접근할 권한에 대한 token이며 다른 하나는 refresh-token으로 최초의 인증 서버로부터의 인증에 성공하였을 때
 * access-token과 함께 제공받는 token이다. access-token은 비교적 수명이 짧으며 수명이 다한 access-token은
 * refresh-token에 의해(refresh-token을 인증 서버에 전달하는 방식으로) 갱신되기 때문에 refresh-token은 일반적으로 수명이 길다.
 * (무한한 수명을 갖는다는 시나리오도 본 적이 있는데 이는 좋지 않다고 생각한다. 혹시라도 공격자가 refresh-token을 취득하게 될 경우 길더라도 
 * 수명을 유한하게 해주어야 취득 된 refresh-token을 무한히 사용하지 않게 할 수 있기 때문이다.)<br>
 * <br>
 * 
 * 그러나 jisang 어플리케이션에서는 단순 인증을 목적으로 JWT token을 사용하는 것이기 때문에 위와 같은 시나리오를 따르지는
 * 않는다.<br>
 * <br>
 * 
 * 
 * JWT token 재생 공격 방지를 위해 초기의 redis 도입 전에는 RDB로부터 JTI를 가져와야 했다. RDB 사용에 따른
 * latency와 RDB의 overhead를 고려하여 토큰 갱신 기간 (기존의 구현에서는 "exp" claim으로 지정한 토큰 만료 시간
 * 외에 추가로 이 보다 더 짧은 갱신 기간용 Date type claim이 존재했었다. 이렇게 갱신 기간을 둘로 나눈 이유는 아래에 나오는데
 * 이 클래스가 사용하는 {@link io.jsonwebtoken}의 기능적 한계 때문이다.)이 지난 JWT token에 대해서만 JTI
 * 검사를 수행하는 것으로 절충 하였었는데, 이 또한 마음에 들지는 않아(갱신 기간 내에 재생 공격이 가능할 수 있다.) 
 * redis를 도입하여 어플리케이션에 어느 정도의 state(JTI)를 추가하기로 하였다.
 * (그나마 다른 정보, user id, user role 등의 상태 유지 정보는 JWT token에 보관된다.) 현재는
 * redis로부터 JTI를 가져오기 때문에 매번 JWT token 인증 마다 JTI 비교를 해도 부담이 덜하게 되었다. 그러나 여기서 새로운
 * 고민이 생겼다. 어플리케이션을 장시간 이용하지 않은 유저에 대한 JTI 마저도 redis 서버의 메모리에 올려 놓는 것이 만족스럽지 않았기
 * 때문이다.<br>
 * <br>
 * 
 * 장시간 미이용 유저에 대한 JTI를 없앨 방법을 고민하였는데, 처음 생각한 방법은 다음과 같다. redis를 도입함으로서 매번 인증때마다
 * redis에 방문하여 JTI를 비교한 후 비교에 성공시 JTI를 새로 업데이트한 토큰을 발급할 것이라고 하였는데, 이 때 마다 토큰의 만료
 * 기간도 같이 업데이트 해주는 것이다. 장기간 미이용 유저의 토큰은 만료가 될 것이고 만료된 토큰에 연관된 redis key를 삭제하기만
 * 하면 된다. 가장 일반적인 방법이겠다. 그러나 이 방법은 jjwt {@link io.jsonwebtoken}을 이용해서는 불가능하다. <br>
 * 이유는 다음과 같다.
 * <br>
 * 
 * {@link io.jsonwebtoken.JwtParser} 구현의 parsing 메서드들은 토큰 만료 기한이 지난 토큰을 전달 받을 경우
 * 바로 예외를 던져버려 만료된 토큰에 대한 user id 등에 접근할 방법이 전혀 없다. 즉, 만료된 토큰 관련 redis key를 구할 수 없다.
 * (JTI 삭제가 불가능하다.)
 * (jjwt에 문의해본 결과 해당 이슈는 처리될 예정이나 현재 JWE가 최우선 작업이라고 한다. 즉 언제 수정될지 알 수 없다. 그러나 만약
 * 이 부분이 수정된다면 방금 말한 방법으로 바꿀 생각인데 그 이유는 아래에 설명하는 다른 방법은 token의 만료 기간 정보가 JWT
 * token에 존재하지 않기 때문이다. 그래도 jjwt를 사용하는 상황에서는 이 방법이 그나마 장기간 미이용 유저의 데이터 삭제에는 가장
 * 좋다.)
 * 
 * 그래서 생각한 다음 방법은 아래와 같다. 매 인증 시 JTI정보에 접근할 때마다 해당 JTI 정보를 담는 redis key의 만료 기한을
 * 늘려주는 것이다. 위 방법의 JWT token의 만료 기간을 매번 늘려주는 것과 결국 같은 이치이다. 어플리케이션의 장기간 미사용 유저의
 * redis key는 만료되버릴 것이다. redis에는 key에 대한 만료 기한을 설정하는 기능이 이미 존재하기 때문에(EXPIRE)
 * redis 선택을 잘 한 것 같다는 생각이 들었다. 다른 DB를 이용했을 경우 일일이 만료 기한이 지난 데이터를 찾아 삭제하도록 스케줄링을
 * 직접 구현해야 하는 불편함이 있으나 redis는 expire만 설정되어 있으면 알아서 삭제 작업을 처리해주기 때문이다. 삭제 작업을
 * redis가 알아서 해결해준다는 점은 분명 이 방법이 더 괜찮아 보이지만 JWT token의 만료 기간을 JWT token이 갖지 않는다는
 * 것은 마음에 들지 않는다. (jjwt 라이브러리가 위에 언급한 부분을 변경하지 않는다는 가정하에, jjwt 사용 중에는 감안해야 한다.)
 * 
 * 그렇다고 custom claim에 만료 시간 정보를 두자니 이 경우 일일이 custom claim으로 부터 만료시간 정보를 추출하여 직접
 * if 문을 사용해 비교하여야 하는 불편이 마음에 들지 않았다. 그래서 결국 두 번째 방법 Redis의 Expired key 방법을
 * 사용하기로 하였다. (exp 클레임을 get하여 if문으로 비교하는 방법도 같은 이유로 마음에 들지 않았다.)
 * 
 * 만료 기한을 갱신하지 않고 고정된 갱신 기한을 사용해도 장기간 미사용자의 redis 데이터가 사라지는 것은 마찬가지이다. 그러나 문제는
 * 이럴 경우 장기간 미사용자의 데이터만 사라지는 것이 아니다. JWT token의 만료 기한이 갱신이 안되면 1분전에도 어플리케이션을 잘
 * 이용하던 유저에게 갑자기 로그인을 해야한다고 알리는 상황이 발생할 수 있다. 이 정도의 불편은 문제 없다면 갱신할 필요는 없겠다. <br>
 * <br>
 * 
 * 
 * 아무튼 위의 방법들 중 아래의 방법을 선택함으로서 장시간 미이용 유저에 대한 정보 삭제 방법을 간구하게 되었다. 또한 JWT token의
 * 만료 기한(+ Redis 키 만료 기한) 이 한 곳에만 존재할 수 있도록 이 클래스의 {@link #buildToken}에 의해 호출되는
 * primitive 메서드인 {@link #getClaims} 메서드는 registered claim "exp"를 설정하지 않는다. 
 * 위에서도 말하였지만 JWT token이 스스로 만료 기한을 담고 있으면 {@link io.jsonwebtoken}라이브러리 이용 상황 하에서는 
 * 재로그인 없이는 만료된 토큰에 대한 JTI 삭제가 불가능하기 때문이기도 하다.(무조건 재로그인해야함.) <br>
 * <br>
 * 
 * 
 * 개인적으로 처음으로 github에 Issue란에 request를 해보았다. 아쉽게도 유사한 주제의 issue가 이미 존재하여 내가 올린
 * Issue는 삭제되었으나 jjwt로부터 답변 메일을 통해 나의 요청이 언젠가는 처리될 것이라는 말을 들으니 다른 곳에도 기여할 수 있겠다는
 * 자신감을 조금 얻었다.<br>
 * <br>
 * 
 * </p>
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class UserAuthJWTService extends AbstractJWTService {

    // Static Fields
    // ==========================================================================================================================

    private static final String USER_CLAIM_NAME = "user";
    private static final String USER_CLAIM_ID_FIELD = "id";
    private static final String USER_CLAIM_ROLE_FIELD = "role";

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * {템플릿 메서드 {@link #buildToken}으로부터 호출되는 메서드로 JWT payload를 구성하는 claims를 생성하여
     * 반환한다. {@link #supports(Class)} 메서드에서 {@link UserAuthTokenDTO} 타입 오브젝트를 지원
     * 가능하다는 사실을 알렸기 때문에 전달받은 인자 {@code tokenDTO}가 {@link UserAuthTokenDTO} 타입 오브젝트일
     * 것이라는 가정하에 바로 형변환을 하였다. 형변환시 오류가 발생하였다면 그것은 이 클래스를 호출하는 쪽의 코드가 잘못된 것이다.
     * 
     */
    @Override
    protected Claims getClaims(TokenDTO tokenDTO) {
        UserAuthTokenDTO userTokenDTO = (UserAuthTokenDTO) tokenDTO;

        Claims claims = Jwts.claims().setId(userTokenDTO.getTokenComponent().getJti());

        claims.put(USER_CLAIM_NAME, userTokenDTO.getAccount());

        return claims;
    }

    /**
     * 이 클래스의 {@code #initValidationStrategy(JwtParser, TokenDTO)} 메서드는 JWT token
     * 검증과 관련하여 아무 작업도 하지않는다.
     */
    @Override
    protected JwtParser initValidationStrategy(JwtParser parser, TokenDTO dto) {
        return parser;
    }

    /**
     * parsing 된 {@code claims} 오브젝트를 기반으로 {@link UserAuthTokenDTO} 오브젝트를 생성하여 반환.
     */
    @Override
    protected UserAuthTokenDTO generateTokenDTO(Claims claims) {
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = claims.get(USER_CLAIM_NAME, LinkedHashMap.class);

        Account account = new Account();
        account.setId((Integer) userMap.get(USER_CLAIM_ID_FIELD));
        account.setRole((String) userMap.get(USER_CLAIM_ROLE_FIELD));

        TokenComponent tokenComponent = new TokenComponent((Integer) userMap.get(USER_CLAIM_ID_FIELD), claims.getId());

        UserAuthTokenDTO tokenDTO = new UserAuthTokenDTO();
        tokenDTO.setAccount(account);
        tokenDTO.setTokenComponent(tokenComponent);

        return tokenDTO;
    }

    /**
     * 이 클래스의 JWT building, parsing에 필요한 정보를 담는 {@link UserAuthTokenDTO} 타입을 지원함을
     * 알리는 메서드이다.
     */
    @Override
    public boolean supports(Class<? extends TokenDTO> tokenDTO) {
        Objects.requireNonNull(tokenDTO,
                "Null value parameter tokenDTO detected while trying to check where can support or not.");

        return UserAuthTokenDTO.class.isAssignableFrom(tokenDTO);
    }
}
