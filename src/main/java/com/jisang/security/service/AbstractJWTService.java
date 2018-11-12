package com.jisang.security.service;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodec;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultJwtBuilder;

import com.jisang.security.dto.TokenDTO;
import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.validation.JWTBuilding;
import com.jisang.security.validation.JWTParsing;
import com.jisang.security.validation.SecurityValidationDelegator;

/**/
/**
 * 
 * JWT building, parsing을 담당하는 {@link JWTService} 인터페이스에 대한 추상 골격 클래스이다.
 *
 * 
 * @author leeseunghyun
 * 
 * 
 */
public abstract class AbstractJWTService implements JWTService {

    // Static fields
    // ==========================================================================================================================

    /** 기본적으로 JWT 헤더는 typ, alg의 두 가지 프로퍼티로 구성되어 있다. */
    protected static final String TYPE_HEADER_NAME = "typ";
    protected static final String ALGORITHM_HEADER_NAME = "alg";

    /** JWT 헤더에는 토큰의 타입(typ)을 나타내는 값으로 일반적으로 그 값으로 "JWT"가 사용되는 것이 추천된다고 한다. */
    protected static final String TYPE_HEADER_VALUE = "JWT";

    protected static final String CLIENT_IP_CLAIM_NAME = "clientIP";
    protected static final String USER_EMAIL_CLAIM_NAME = "userEmail";

    // Instance fields
    // ==========================================================================================================================

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${jwt.signature.secretkey}")
    protected String secretKey;
    @Value("${jwt.signature.algorithm}")
    protected String algorithm;
    @Value("${jwt.signature.compressioncodec}")
    protected String compressionCodec;

    @Autowired
    private SecurityValidationDelegator validator;

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * JWT token 생성에 대한 템플릿 메서드로 전달 받은 {@link UserAuthTokenDTO}의 정보를 바탕으로 JWT token
     * 문자열을 생성한다. JWT 생성 전략에 따라 변할 수 있는 claims 생성은 이 템플릿 메서드가 호출하는
     * {@link #getClaims(UserAuthTokenDTO)}를 호출하여 수행한다.
     * 
     * @param dto
     *            JWT token 구성에 필요한 정보가 담긴 오브젝트.
     * @return 생성된 JWT token 문자열.
     * @throws InternalAuthenticationServiceException
     *             토큰 building 과정 중 {@link SignatureException},
     *             {@link CompressionException} 이 발생하였을 경우 이 예외가 던져진다. 이들 예외는
     *             property 설정 파일에 설정이 잘못되어 발생할 가능성이 크다.
     * 
     */
    @Override
    public String buildToken(TokenDTO tokenDTO) {
        Objects.requireNonNull(tokenDTO, "Invalid null value tokenDTO detected while trying to build JWT.");

        validator.validate(tokenDTO, JWTBuilding.class);

        String jwt = getJwtBuilder().setHeaderParam(TYPE_HEADER_NAME, TYPE_HEADER_VALUE)
                .setHeaderParam(ALGORITHM_HEADER_NAME, algorithm)

                .setClaims(getClaims(tokenDTO))

                .signWith(SignatureAlgorithm.forName(algorithm), secretKey)
                // .compressWith(resolveCompressionCodec(compressionCodec))
                .compact();
        return jwt;
    }

    /**
     * 
     * JWT token의 파싱을 담당한다. 이 메서드의 파라미터와 리턴 타입이 모두 {@link UserAuthTokenDTO} 타입임을 알 수
     * 있는데, 메서드 인자 {@link UserAuthTokenDTO}와 반환되는 {@link UserAuthTokenDTO}는 내부적으로 담고
     * 있는 정보가 다르다.
     * 
     * @param tokenDTO
     *            JWT token 파싱 과정 중 토큰 검증 작업{@link #initValidationStrategy}에 사용 된다.
     *            {@link HttpServletRequest}로부터 전달 받은 jwt token 등의 프로퍼티를 포함하고 있으며
     *            현재는 이용하지 않지만 만약 사용자 IP address 정보에 대한 비교가 토큰 검증에 필요할 경우
     *            {@code HttpServletRequest#getRemoteAddr()}와 같은 메서드를 통해 전달받은 값을 갖고
     *            있을 것이다.
     * @return parsing 된 토큰의 정보를 담은 TokenDTO.
     * 
     * @throws InternalAuthenticationServiceException
     *             인자에 대한 검증에 실패하였을 경우 던져진다.
     * @throws IncorrectClaimException
     *             claim의 값이 기대한 값과 다를 경우 던져진다.
     * @throws MissingClaimException
     *             검사하려는 claim이 존재하지 않을 경우 던져진다.
     * @throws JwtException
     *             {@link JwtParser#parseClaimsJws(String)} 현재 구현에서
     *             {@link JwtException}타입의 예외들은 spring aop를 이용해 변환된다.
     * 
     */
    @Override
    public TokenDTO parseToken(TokenDTO tokenDTO) {
        Objects.requireNonNull(tokenDTO, "Invalid null value tokenDTO detected while trying to parse token.");

        validator.validate(tokenDTO, JWTParsing.class);

        JwtParser parser = initValidationStrategy(Jwts.parser().setSigningKey(secretKey), tokenDTO);
        Claims claims = parser.parseClaimsJws(tokenDTO.getToken()).getBody();
        return generateTokenDTO(claims);
    }

    @Override
    public TokenDTO parseToken(String jwtToken) {
        Objects.requireNonNull(jwtToken, "Invalid null value tokenDTO detected while trying to parse token.");

        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody();
        return generateTokenDTO(claims);
    }

    /**
     * 
     * @param codecName
     *            compression 알고리즘을 나타내는 문자열이다.
     * @return 인자로 전달된 codec name에 해당하는 io.jsonwebtoken의 CompressionCodec을 반환한다.
     * @throws InternalAuthenticationServiceException
     *             - property 설정 파일로부터 읽어 온 {@code codecName}이 잘못 되었을 때 이 예외가 던져진다.
     * 
     */
    protected CompressionCodec resolveCompressionCodec(String codecName) {
        Objects.requireNonNull(codecName);
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving compression codec...");
        }
        String codecNameUpperCase = codecName.toUpperCase();
        switch (codecNameUpperCase) {
        case "DEFLATE":
            return CompressionCodecs.DEFLATE;
        case "GZIP":
            return CompressionCodecs.GZIP;
        default:
            throw new InternalAuthenticationServiceException("Unsupported JWT compression codec name : " + codecName);
        }
    }

    /**
     * 이 추상 클래스의 구현이 필요할 경우 오버라이드하여 커스텀 {@link JwtBuilder}를 반환할 수 있도록 구현 된 hook
     * 메서드이다. 기본 구현은 {@link Jwts#builder()}가 반환하는 {@link DefaultJwtBuilder} 오브젝트이다.
     */
    public JwtBuilder getJwtBuilder() {
        return Jwts.builder();
    }

    // Abstract Methods
    // ==========================================================================================================================

    /**
     * 
     * JWT payload를 이루는 claim들을 생성한다. 이 메서드는 이 클래스의 {@link #buildToken} 메서드로부터 호출되는
     * primitive 메서드인데, claims 생성 전략이 쉽게 변할일은 없기에 이렇게 템플릿 메서드 패턴을 적용하기에 적절치 않다고 보일
     * 수도 있다. 그러나 특정 기간(예 : 이벤트) 동안만 인증에 필요한 정보가 바뀐다거나 하는 일은 충분히 가능한 시나리오라고 생각하여 이렇게
     * 구현하였다.
     * 
     * 위와 같은 이유로 처음에는 claims에 대한 factory 클래스를 정의하였었는데, factory 클래스로부터 claims를 받아오는
     * 방법을 결과적으로 사용하지 않은 이유는 다음과 같다. claims 생성 전략이 바뀔 경우 그 claims를 validation하는 전략 및
     * DTO오브젝트로의 파싱 전략이 바뀌기 마련인데, claims 생성만 다른 클래스에게 맡길 경우 토큰 검증 작업과 파싱 작업과의 일관성에
     * 문제가 발생할 수 있기 때문이다. 그러므로 이 클래스에는 이 메서드 뿐만 아니라 JWT token을 구성하는 claims에 대한 검증
     * 정책을 설정하는 추상 메서드 {@link #initValidationStrategy, #generateTokenDTO}가 선언되어 있다.
     * 
     */
    protected abstract Claims getClaims(TokenDTO dto);

    /**
     * 
     * 토큰에 대한 검증 정책을 설정. 이 메서드는 {@link #getClaims} 메서드와 일관되게 동작하도록 구현되어야 한다. 다시 말해,
     * 이 메서드는 {@link #getClaims} 메서드가 생성한 claims 중 검증이 필요한 요소가 있을 경우 해당 요소에 대한 검증
     * 정책을 반드시 설정하여야 한다.
     * 
     * 
     * <pre>
     *  사용 예 : {@code return parser.requireIssuer(tokenDTO.getIssuer()).requireSubject(tokenDTO.getSubject())...;}
     * </pre>
     * 
     * @return 토큰 검증 전략이 설정된 {@link JwtParser} 오브젝트를 반환한다.
     * 
     */
    protected abstract JwtParser initValidationStrategy(JwtParser parser, TokenDTO tokenDTO);

    /**
     * 
     * 파싱 된 {@code claim}을 바탕으로 클라이언트가 원하는 오브젝트({@link UserAuthTokenDTO})를 생성해 반환한다.
     * {@link #getClaims} 메서드와 일관되게 동작하도록 구현되어야 한다. 다시 말해, 이 메서드는 {@link #getClaims}
     * 메서드가 생성한 claims를 올바로 담은 {@link UserAuthTokenDTO} 오브젝트를 반환해야 한다.
     * 
     * 
     * @return 토큰 검증 전략이 설정된 {@link JwtParser} 오브젝트를 반환한다.
     * 
     */
    protected abstract TokenDTO generateTokenDTO(Claims claims);

}
