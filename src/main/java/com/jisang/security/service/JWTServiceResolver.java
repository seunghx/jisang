package com.jisang.security.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import com.jisang.security.dto.TokenDTO;

/**
 * 
 * JWT token의 building, parsing에 사용 될 {@link JWTService} 구현 오브젝트를 선택하는 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class JWTServiceResolver {

    // Instance fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(JWTServiceResolver.class);
    private final List<JWTService> jwtServices;

    // Constructors
    // ==========================================================================================================================

    public JWTServiceResolver(List<JWTService> jwtServices) {

        this.jwtServices = jwtServices;

        if (Objects.isNull(this.jwtServices) || this.jwtServices.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Received invalid argument. Argument jwtServices : {}", jwtServices);
            }
            throw new IllegalArgumentException("Argument jwtServices is null or empty list.");
        }
    }

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 인자 {@code tokenDTO}를 기반으로 JWT building, parsing을 담당할 {@link JWTService} 구현을
     * 선택한다.
     * 
     * @return 선택된 {@link JWTService} 구현 오브젝트를 반환한다.
     * 
     */
    public JWTService resolveJWTService(Class<? extends TokenDTO> tokenDTO) {
        Optional.ofNullable(tokenDTO).orElseThrow(() -> {
            logger.warn("Argument tokenDTO is null. Checking code required.");

            throw new InternalAuthenticationServiceException("Argument tokenDTO is null.");
        });

        try {
            return jwtServices.stream()
                              .filter(jwtService -> jwtService.supports(tokenDTO))
                              .findFirst()
                              .get();
            
        } catch (NoSuchElementException e) {
            logger.warn("Unsupported TokenDTO type argument detected. Argument tokenDTO : {}", tokenDTO);

            throw new InternalAuthenticationServiceException("Unsupported TokenDTO object detected.", e);
        }
    }
}
