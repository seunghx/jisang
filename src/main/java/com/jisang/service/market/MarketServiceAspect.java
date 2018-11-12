package com.jisang.service.market;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
 * {@link MarketService} 구현 클래스에 대한 애스팩트를 정의한 클래스.
 *
 *
 * 현재 구현은 {@link MarketService} 구현 클래스의 메서드의 시작과 끝을 알리는 로깅만이 정의되어 있다. 각각의 서비스
 * 오브젝트의 메서드는 각각의 비즈니스 로직을 의미하므로 비즈니스 로직의 시작과 끝을 알린다는 의미로 info 레벨의 로깅을 수행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Component
@Order(2)
public class MarketServiceAspect {

    private final Logger logger = LoggerFactory.getLogger(MarketServiceAspect.class);

    @Around("execution(* com.jisang.service.market.*.modifyMarket(..))")
    public Object marketModificationLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to modify market info by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Market info modification succeeded");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Market info modification failed.");
            }

            throw e;
        }
    }

    @Around("execution(* com.jisang.service.market.*.findMarketForManagement(..))")
    public Object findingMarketForManagementLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to finding market info for management by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Finding market info succeeded.");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Finding market info failed.");
            }

            throw e;
        }
    }
}
