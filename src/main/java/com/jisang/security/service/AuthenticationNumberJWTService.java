package com.jisang.security.service;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;

import com.jisang.security.dto.AuthenticationNumberTokenDTO;
import com.jisang.security.dto.TokenDTO;
import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.exception.AuthenticationNumberExpiredException;

/**
 * 
 * 인증 번호 발급에 사용될 {@link AbstractJWTService} 구현(추상 클래스 계승) 클래스이다. 이 클래스가 생성하는 JWT
 * token에는 유저에게 전달되는 인증 번호 값이 저장되어 있는데, jisang 어플리케이션은
 * {@link HttpServletSession}과 같은 세션을 갖지 않는 어플리케이션이기 때문에 인증 번호 검사를 위해 JWT token에
 * 인증 번호를 저장하게 되었다. 이 클래스의 필드 {@code authenticationNumberTTL}은 일반적으로 매우 짧은(예를 들어
 * 2분)시간이 지나면 만료되어야 하는데, 그렇게 되야 몇 분안에 전송된 인증번호를 입력하라는 요구사항을 충족시킬 수 있다.
 * 
 * @author leeseunghyun
 *
 */
public class AuthenticationNumberJWTService extends AbstractJWTService {

    // Static Fields
    // ==========================================================================================================================

    private static final String AUTH_NUMBER_CLAIM_NAME = "authenticationNumber";

    // Instance Fields
    // ==========================================================================================================================

    @Value("${jwt.token.authentication-number.ttl}")
    private long authenticationNumberTTL;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    @Override
    public TokenDTO parseToken(TokenDTO tokenDTO) {
        Objects.requireNonNull(tokenDTO, "Null value parameter tokenDTO detected while trying to parse JWT token.");

        try {
            return super.parseToken(tokenDTO);

        } catch (ExpiredJwtException e) {
            if (logger.isDebugEnabled()) {
                logger.info("JWT token consist of authentication number expired.");
                logger.info("Converting {} to AuthenticationNumberExpiredException...", e.toString());
            }

            throw new AuthenticationNumberExpiredException(e.getMessage(), e);
        }
    }

    /**
     * 
     * 인증 번호를 claim으로 설정한다. 또한 클라이언트의 IP 주소를 claim으로 설정함을 알 수 있는데 일반적으로 몇 분의 짧은 시간
     * 내로 수행되는 인증 번호 입력 절차 중에 IP 주소가 변경될 일은 없을 것이다. 그러나 만약 다른 IP로 해당 토큰이 전달된다면 분명
     * 정상적인 요청이라고 볼 수 없기 때문에 최소한의 방어책으로 IP주소를 입력받게 하였다. 일반적인 인증에는 IP검사가 적절치 않은데,
     * 장시간의 자동 로그인이 유지되게 하려면 요청 IP 검사를 진행하면 안되기 때문이다.
     * 
     */
    @Override
    protected Claims getClaims(TokenDTO tokenDTO) {
        AuthenticationNumberTokenDTO authNumberTokenDTO = (AuthenticationNumberTokenDTO) tokenDTO;

        Claims claims = Jwts.claims().setExpiration(Date.from(Instant.now().plusSeconds(authenticationNumberTTL)));

        claims.put(CLIENT_IP_CLAIM_NAME, authNumberTokenDTO.getClientIPAddr());
        claims.put(AUTH_NUMBER_CLAIM_NAME, authNumberTokenDTO.getAuthenticationNumber());
        claims.put(USER_EMAIL_CLAIM_NAME, authNumberTokenDTO.getUserEmail());

        claims.setExpiration(Date.from(Instant.now().plusSeconds(authenticationNumberTTL)));

        return claims;
    }

    /**
     * 유저가 전송한 인증 번호와 인증 번호를 전송한 유저의 ip를 비교한다.
     */
    @Override
    protected JwtParser initValidationStrategy(JwtParser parser, TokenDTO tokenDTO) {
        return parser.require(CLIENT_IP_CLAIM_NAME, ((AuthenticationNumberTokenDTO) tokenDTO).getClientIPAddr())
                .require(AUTH_NUMBER_CLAIM_NAME, ((AuthenticationNumberTokenDTO) tokenDTO).getAuthenticationNumber());

    }

    /**
     * 사용자의 요청 파라미터와의 비교를 위해 JWT token 설정 해둔 인증 번호를 담은 {@link UserAuthTokenDTO}를
     * 반환한다.
     */
    @Override
    protected AuthenticationNumberTokenDTO generateTokenDTO(Claims claims) {
        AuthenticationNumberTokenDTO authNumberTokenDTO = new AuthenticationNumberTokenDTO();

        authNumberTokenDTO.setUserEmail(claims.get(USER_EMAIL_CLAIM_NAME, String.class));
        return authNumberTokenDTO;
    }

    /**
     * 이 클래스가 인증 번호와 관련된 JWT token의 building, parsing을 처리함에 필요한 정보를 담는
     * {@link AuthenticationNumberTokenDTO} 타입 오브젝트를 지원함을 알리는 메서드이다.
     */
    @Override
    public boolean supports(Class<? extends TokenDTO> tokenDTO) {
        Objects.requireNonNull(tokenDTO,
                "Null value parameter tokenDTO detected while trying to check where can support or not.");

        return AuthenticationNumberTokenDTO.class.isAssignableFrom(tokenDTO);
    }
}
