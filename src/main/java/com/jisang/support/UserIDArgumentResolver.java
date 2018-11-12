package com.jisang.support;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.jisang.security.core.DefaultUserDetailsAuthentication;

/**
 * 
 * 
 * 유저 정보 조회를 위해선 seucirty 단에서의 JWT 파싱으로 생성된 유저 정보가 필요하다. 이 정보는
 * {@link Authentication} 오브젝트를 통해서 전달 받을 수 있는데, {@link Authentication} 타입 오브젝트가
 * 예상과 다른 오브젝트일 경우 올바른 동작이 불가능하기 때문에 이를 검사하는게 안전다하고 생각하여 아래와 같이 구체적인
 * {@link Authentication} 구현 오브젝트에 대한 타입 검사를 수행한다. (혼자 만들었기 때문에 어플리케이션이 어떻게
 * 동작할지야 다 알고 있고 혼자 만든게 아니더라도 어떤 타입의 {@link Authentication} 구현 오브젝트가 전달 될지는 알 수
 * 있겠지만, 그래도 아래와 같이 타입에 대한 검사를 수행해주는 것이 혹시 모를 변경({@link Authentication} 구현 오브젝트의
 * 변경)에 따른 위험을 방지할 수 있을 것 같다.)
 * 
 * 
 * 기존 인증이 필요한 url 처리를 담당하는 핸들러 메서드들에서는 {@link Authentication} 타입 파라미터를 전달 받은 후
 * 아래와 같은 로직을 이용해 유저의 id를 받아왔었다.
 * 
 * <pre>
 * int userId = Optional.ofNullable(authentication).filter(auth -> auth instanceof DefaultUserDetailsAuthentication)
 *         .map(auth -> ((DefaultUserDetailsAuthentication) auth).getPrincipal().getId()).orElseThrow(() -> {
 *             logger.warn("Something is wrong. authentication must be instance of {} but {}",
 *                     DefaultUserDetailsAuthentication.class, authentication);
 * 
 *             throw new IllegalStateException("Illegal authentication detected.");
 *         });
 * </pre>
 * 
 * 매번 각각의 핸들러 메서드에서 위의 로직을 수행하기 때문에 코드의 중복이 많이 심했고 그런 이유로
 * {@link HandlerMethodArgumentResolver}를 구현한 이 클래스를 정의하게 되었다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class UserIDArgumentResolver implements HandlerMethodArgumentResolver {

    private final Logger logger = LoggerFactory.getLogger(UserIDArgumentResolver.class);

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Objects.requireNonNull(parameter,
                "Null value method parameter detected while tyring to check paramter supportness");

        return UserID.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * @param parameter
     *            - {@link #supportsParameter}에 의해 {@link Integer} 타입임이 분명하며 null
     *            검사도 이미 진행되었다.
     */
    @Override
    public UserID resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        logger.debug("Starting to resolving argument.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // null is not instanceof DefaultUserDetailsAuthentication
        if (authentication instanceof DefaultUserDetailsAuthentication) {
            DefaultUserDetailsAuthentication auth = (DefaultUserDetailsAuthentication) authentication;

            if (auth.getPrincipal() == null) {
                logger.error("Somethis's wrong. Principal of Authentication object is null.");

                throw new IllegalStateException("Illegal principal exists in authentication. Fixing required.");
            } else if (auth.getPrincipal().getId() == null) {
                logger.error("Somethis's wrong. User ID of Principal is null.");

                throw new IllegalStateException("Illegal principal exists in authentication. Fixing required.");
            }

            logger.debug("Authentication is valid. Now start returnning argument...");

            return new UserID(auth.getPrincipal().getId());
        } else {
            logger.error("Something's wrong. Received Authentication object is invalid.");
            logger.error("Authentication must be instance of {} but {}", DefaultUserDetailsAuthentication.class,
                    authentication);

            throw new IllegalStateException("Illegal Authentication object detected.");
        }

    }

}
