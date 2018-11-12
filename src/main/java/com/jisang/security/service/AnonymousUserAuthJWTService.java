package com.jisang.security.service;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

import com.jisang.security.dto.AnonymousUserAuthTokenDTO;
import com.jisang.security.dto.TokenDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class AnonymousUserAuthJWTService extends AbstractJWTService {

    // Static Fields
    // ==========================================================================================================================

    private static final String AUTHENTICATED_CLAIM_NAME = "authenticated";

    // Instance Fields
    // ==========================================================================================================================

    @Value("${jwt.token.authenticated-token.ttl}")
    private long authenticatedTokenTTL;
    // Methods
    // ==========================================================================================================================

    @Override
    protected Claims getClaims(TokenDTO tokenDTO) {

        AnonymousUserAuthTokenDTO anonymousAuthTokenDTO = (AnonymousUserAuthTokenDTO) tokenDTO;

        Claims claims = Jwts.claims().setExpiration(Date.from(Instant.now().plusSeconds(authenticatedTokenTTL)));

        claims.put(CLIENT_IP_CLAIM_NAME, anonymousAuthTokenDTO.getClientIPAddr());
        claims.put(USER_EMAIL_CLAIM_NAME, anonymousAuthTokenDTO.getUserEmail());
        claims.put(AUTHENTICATED_CLAIM_NAME, true);

        return claims;
    }

    @Override
    protected JwtParser initValidationStrategy(JwtParser parser, TokenDTO tokenDTO) {
        AnonymousUserAuthTokenDTO anonymousAuthTokenDTO = (AnonymousUserAuthTokenDTO) tokenDTO;

        return parser.require(CLIENT_IP_CLAIM_NAME, anonymousAuthTokenDTO.getClientIPAddr())
                     .require(AUTHENTICATED_CLAIM_NAME, true);
    }

    /**
     * 이 {@link JWTService} 구현의 {@code #generateTokenDTO} 메서드는 인증된 사용자의 주소가 담긴
     * {@link AnonymousUserAuthTokenDTO}를 반환한다.
     */
    @Override
    protected TokenDTO generateTokenDTO(Claims claims) {

        AnonymousUserAuthTokenDTO tokenDTO = new AnonymousUserAuthTokenDTO();
        tokenDTO.setUserEmail(claims.get(USER_EMAIL_CLAIM_NAME, String.class));

        return tokenDTO;
    }

    @Override
    public boolean supports(Class<? extends TokenDTO> tokenDTO) {
        Objects.requireNonNull(tokenDTO,
                    "Null value parameter tokenDTO detected while trying to check where can support or not.");

        return AnonymousUserAuthTokenDTO.class.isAssignableFrom(tokenDTO);
    }
}
