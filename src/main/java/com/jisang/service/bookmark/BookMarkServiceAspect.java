package com.jisang.service.bookmark;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 
 * {@link BookMarkService} 구현 클래스에 대한 애스팩트를 정의한 클래스.
 *
 * 
 * 현재 구현은 {@link BookMarkService} 구현 클래스의 메서드의 시작과 끝을 알리는 로깅만이 정의되어 있다. 각각의 서비스
 * 오브젝트의 메서드는 각각의 비즈니스 로직을 의미하므로 비즈니스 로직의 시작과 끝을 알린다는 의미로 info 레벨의 로깅을 수행한다.
 * 
 *
 * @author leeseunghyun
 * 
 */
@Aspect
@Component
@Order(2)
public class BookMarkServiceAspect {

    private final Logger logger = LoggerFactory.getLogger(BookMarkServiceAspect.class);

    @Around("execution(* com.jisang.service.bookmark.*.modifyBookMark(..))")
    public Object modifyBookmarkLog(ProceedingJoinPoint jp) throws Throwable {

        if (logger.isInfoEnabled()) {
            logger.info("Starting to modify bookmark by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Modifying bookmark succeeded.");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Modifying bookmark failed.");
            }

            throw e;
        }
    }

    @Around("execution(* com.jisang.service.bookmark.*.deleteBookMark(..))")
    public Object deletingBookmarkLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to delete bookmark by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Bookmark deletion succeeded.");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Bookmark deletion failed.");
            }

            throw e;
        }
    }

    @Around("execution(* com.jisang.service.bookmark.*.isBookMarked(..))")
    public Object isBookmarkedLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to find bookmark info by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Finding bookmark info succeeded.");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Finding bookmark info failed.");
            }

            throw e;
        }
    }

    @Around("execution(* com.jisang.service.bookmark.*.findBookmarks(..))")
    public Object findingBookMarksLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to find bookmark list by {}", jp.getTarget());
        }

        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Finding bookmark list succeeded.");
            }

            return result;
        } catch (Exception e) {

            if (logger.isInfoEnabled()) {
                logger.info("Finding bookmark list failed.");
            }

            throw e;
        }
    }
}
