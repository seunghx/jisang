package com.jisang.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 
 * 메서드의 시작과 끝을 알리는 debugging용 로깅을 한다. 
 * 
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Order(1)
@Component
public class MethodLoggingAspect {
	private final Logger logger = LoggerFactory.getLogger(MethodLoggingAspect.class);
	
	@Around("execution(* com.jisang..*.*(..)) && !execution(* com.jisang.security..*.*(..))"
			 							  + " && !execution(* com.jisang.config..*.*(..))"
										  + " && !execution(* com.jisang.persistence..*.*(..))"
			 							  + " && !execution(* com.jisang.web..*.*(..))")
	public Object methodLog(ProceedingJoinPoint jp) throws Throwable {
		if(logger.isDebugEnabled()) {
			logger.debug("Starting method {}#{}.", jp.getTarget(), jp.getSignature().toShortString());
		}
		try {
			Object result = jp.proceed();
			
			if(logger.isDebugEnabled()){
				logger.debug("Method #{} finished with returnning : {}.", jp.getSignature(), result);
			}			
			return result;
		}catch(Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("method #{} throwed exception : {}.", jp.getSignature(), e.toString());
			}
			throw e;
		}
	}
}
