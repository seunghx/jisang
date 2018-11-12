package com.jisang.security.authentication.provider;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.jisang.security.core.UserAuthTokenDTOAuthentication;
import com.jisang.security.domain.TokenComponent;
import com.jisang.security.dto.UserAuthTokenDTO;
import com.jisang.security.exception.JWTAuthenticationException;
import com.jisang.security.persistence.RedisUserDAO;
import com.jisang.security.validation.JWTBasedUserAuthentication;
import com.jisang.security.validation.SecurityValidationDelegator;

/**/
/**
 * 
 * 전달받은 {@link UserAuthTokenDTO} 오브젝트를 기반으로 인증을 수행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Component("tokenProvider")
public class JWTTokenAuthenticationProvider implements AuthenticationProvider {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RedisUserDAO userDAO;
    @Autowired
    private SecurityValidationDelegator validator;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * JWT token이 유효한 토큰인지를 알기 위하여 JTI에 대한 비교가 필요한데, 매 인증마다 JTI 비교를 위해 데이터베이스로부터 유저
     * 정보를 받아오는 것은 오버헤드가 클 것이라고 생각되어 토큰 갱신 기간이 지난 토큰에 대해서만 JTI 비교를 수행하기로 결정하였다.
     * 
     * 기존 RDB 사용시 지연 시간에 대한 부담 완화를 위해 갱신 기간이 지난 토큰에 대해서만 JTI 비교를 하였는데, 현재는 JTI를
     * redis로부터 가져오기 때문에 지연 시간에 대한 문제는 어느 정도 완화되었으므로 JTI를 갱신 기간마다 검사할 필요가 없다. 그러나
     * 일단은 갱신 기간 마다 JTI를 검사하겠다는 정책은 유지하기로 하였다. 갱신 기간을 기존 30분에서 10분 정도로 더 줄이면 보다 더
     * 안전할 수 있겠다.
     * 
     * 이 메서드를 보면 random byte 배열을 생성하고 있음을 알 수 있다. {@link Random#nextBytes}메서드는 내부적으로
     * {@link Random#nextInt} 메서드를 호출하는데
     * 
     * 
     * 
     * @return 인증에 성공하였을 경우 JWT token 정보를 담은 {@link UserAuthTokenDTO} 객체가 담긴
     *         {@link UserAuthTokenDTOAuthentication} 오브젝트를 반환.
     * @throws InternalAuthenticationServiceException
     *             인자로 전달받은 {@code authentication}이 null일 경우 발생한다. {@link #supports}
     *             메서드에서 true를 반환하여야 이 메서드가 호출될 것이기 때문에 {@code authentication}의 타입
     *             검사는 진행하지 않았다.
     * @throws BadCredentialsException
     *             {@link #authenticateInternal(UserAuthTokenDTO)}
     * 
     */
    @Override
    public UserAuthTokenDTOAuthentication authenticate(Authentication authentication) throws AuthenticationException {

        UserAuthTokenDTO tokenDTO = Optional.ofNullable(authentication).filter(auth -> auth.getPrincipal() != null)
                .map(auth -> ((UserAuthTokenDTOAuthentication) auth).getPrincipal()).orElseThrow(() -> {
                    logger.warn("Illegal argument detected. received unsupported Authentication object : {}",
                            authentication);
                    return new InternalAuthenticationServiceException("Illegal Authentication argument detected.");
                });

        authenticateInternal(tokenDTO);

        tokenDTO.getTokenComponent().setJti(UUID.randomUUID().toString().replace("-", ""));

        Executor executor = Executors.newCachedThreadPool();
        executor.execute(() -> userDAO.update(tokenDTO.getTokenComponent()));

        return new UserAuthTokenDTOAuthentication(tokenDTO);
    }

    /**
     * 
     * 토큰의 갱신 기간이 지났을 경우에만 호출되는 메서드로 JTI 비교를 통한 인증을 수행한다. 인증에 실패할 경우 예외를 던진다.
     * 
     * @throws BadCredentialsException
     *             JWT token이 담고 있는 JTI 데이터가 데이터베이스로부터 가져온 유저의 JTI와 다를 경우 던져진다. 또한
     *             {@link TokenDTOBasedUserDetailsService#loadUserByUsername} 메서드 수행
     *             중 전달 받은 유저 이름에 해당하는 유저 정보가 데이터베이스에 없을 경우 이는 토큰에 담긴 정보가 옳지 않음을
     *             나타내므로 이 예외가 던져진다. 이런 상황은 JWT token를 signing에 사용되는 secret key가
     *             유출되지 않은 경우 삭제 된 유저에 대한 JWT 토큰의 만료기간이 지나지 않았는데 누군가가 이 토큰을 이용해 접속을
     *             시도했을 때 발생할 수 있다.
     * 
     */
    private void authenticateInternal(UserAuthTokenDTO tokenDTO) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting JTI validation...");
        }

        validator.validate(tokenDTO, JWTBasedUserAuthentication.class);

        try {
            TokenComponent tokenComponent = userDAO.find(tokenDTO.getAccount().getId());

            if (!tokenDTO.getTokenComponent().getJti().equals(tokenComponent.getJti())) {
                logger.warn("Illegal JWT token detected! : JTI is invalid. JTI : {}",
                        tokenDTO.getTokenComponent().getJti());
                throw new JWTAuthenticationException("Invalid JTI detected");
            }
        } catch (UsernameNotFoundException e) {
            logger.warn("Illegal JWT token detected. username does not exist.");
            throw e;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("JTI validation succeeded...");
        }
    }

    /**
     * 전달 받은 {@link Class} object가 인증 과정을 위해 이 클래스가 처리할 수 있는 오브젝트인지를 판단한다. 이 메서드 구현은
     * {@code authentication}이 {@link TokenDTOBAsedAuthentication} 타입 오브젝트일 경우에
     * true를 반환한다.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UserAuthTokenDTOAuthentication.class.isAssignableFrom(authentication);
    }
}
