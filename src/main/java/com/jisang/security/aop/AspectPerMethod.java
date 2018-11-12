package com.jisang.security.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
 * {@code com.jisang.security} 패키지 하위의 모든(예외 존재) 클래스의 메서드에 적용 되는 Aspect 구현
 * 클래스이다.
 * 
 * 인자에 대한 null 검사는 오브젝트를 사용하는 쪽에서 하는 것이 가장 안전하다는 생각에 생략하였다.
 *
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Order(1)
@Component
public class AspectPerMethod {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 메서드 시작과 끝을 알리는 로깅 작업을 대신 수행해주는 애스팩트로 debug 레벨(메서드의 시작, 끝은 debug레벨이 적절한 것
     * 같다.)의 로깅을 수행한다. 포인트 컷 표현식을 보면 인증 필터는 제외가 된 것을 알 수 있다. 조인 포인트로 선정이 되었음에도 인증
     * 필터에는 아래의 로깅 기능이 적용이 되지 않아 그냥 포인트컷 표현식을 통해 아예 에스팩트 적용 대상에서 제외시키기로 하였다. 인증 필터의
     * 메서드에 경우 public 메서드라도 같은 빈의 다른 메서드(예를 들면 {@code #doFilter()} 메서드)로부터 호출되기 때문에
     * proxy의 대상이 되지 않는다.
     * 
     */
    @Around("within(com.jisang.security..*) && !execution(* com.jisang.security.authentication.*Filter.*(..))")
    public Object securityMethodLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting method {}#{}.", jp.getTarget(), jp.getSignature().toShortString());
        }

        try {
            Object result = jp.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} finished with returnning : {}.", jp.getSignature(), result);
            }
            return result;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} throwed exception : {}.", jp.getSignature(), e.toString());
            }
            throw e;
        }
    }
}
