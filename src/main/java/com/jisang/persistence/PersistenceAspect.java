package com.jisang.persistence;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

/**/
/**
 * 
 * DAO 오브젝트에서 다뤄야 할 로깅 작업에 대한 애스팩트(메서드)를 정의한 클래스이다. 현재 jisang 어플리케이션은 관계형
 * 데이터베이스로 MySQL을 사용하고 있는데, MySQL(현재 mybatis sql mapper 이용)에 접근하는 다른 DAO들은
 * log4jdbc를 통한 로깅 기능이 적용되어 있는 상태이다. 그러나 {@link S3MultipartDAO}같은 클라우드 리파지토리에 대한
 * DAO 클래스에는 log4jdbc와 같은 외부 라이브러리가 로깅을 대신 수행해줄 수 없으므로 직접 로깅 작업을 구현해야한다. debug
 * 레벨의 보다 구체적인 로깅은 메서드 내부에서 수행하며 이 클래스의 Aspect 메서드 들에서는 보다 큼지막한 단위의 액션에 대한 INFO
 * 레벨 로깅을 수행한다.
 * 
 * 위에서 INFO 레벨의 로깅을 한다고 하였으나 {@link #s3UploadLog(ProceedingJoinPoint)}와
 * {@link #s3DeleteLog(ProceedingJoinPoint)}에는 지상 어플리케이션의 다른 모든 패키지의 메서드들과 일관되게
 * 로깅이 적용되게 하기 위하여 메서드의 시작과 끝을 알리는 DEBUG 레벨의 로깅 또한 존재한다. 다른 패키지에는
 * {@link com.jisang.aop.MethodLoggingAspect}에 의하여 앞서 말한 DEBUG 레벨 로깅이 적용되나
 * com.jisang.persistence 패키지는 DEBUG 로깅의 대상이 아니다. 이유는 이 패키지의
 * {@link S3MultipartDAO}를 제외한 나머지 대부분의 DAO 클래스는 초반부에 말하였듯이 log4jdbc를 통해 디테일 로깅이
 * 이미 제공되고 있기 때문이다.
 * 
 * 전달되는 오브젝트에 대한 null 검사를 대신 진행해주면 이 애스팩트 클래스의 메서드에 의해 AOP가 적용되는 메서드에서 일일이 null
 * 검사를 하지 않아도 된다. 코드의 중복을 피할 수 있겠으나 null 검사는 오브젝트를 직접 사용하는 쪽에서 수행하는 것이 가장 안전한
 * 방법인 것 같다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Component
@Aspect
public class PersistenceAspect {
    private final Logger logger = LoggerFactory.getLogger(PersistenceAspect.class);

    /**
     * 
     * S3 업로드 기능을 담당하는 DAO 메서드에 적용되는 로깅 애스팩트. 로깅
     * 
     * @throws DataAccessException
     *             AWS S3 연산 중 {@link AmazonClientException} 타입 예외가 발생할 경우 이를 변환하여
     *             {@link DataAccessException}이 던져진다.
     * 
     */
    @Around("execution(* com.jisang.persistence.S3MultipartDAO.upload*(..))")
    public Object s3UploadLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting method {}#{}.", jp.getTarget(), jp.getSignature().toShortString());
        }
        if (logger.isInfoEnabled()) {
            logger.info("Starting to upload files to S3");
        }
        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Uploading files to S3 suceeded.");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} finished with returnning : {}.", jp.getSignature(), result);
            }
            return result;
        } catch (AmazonClientException e) {
            String exceptionMessage = null;

            if (e instanceof AmazonServiceException) {
                AmazonServiceException ex = (AmazonServiceException) e;
                exceptionMessage = ex.getErrorCode() + "-" + ex.getErrorMessage() + " by" + ex.getErrorType()
                        + "'s fault.";
            } else {
                exceptionMessage = e.getMessage();
            }

            DataAccessException dataAccessException = new DataAccessException(exceptionMessage, e) {

                private static final long serialVersionUID = 3920413097909327731L;
            };

            if (logger.isInfoEnabled()) {
                logger.info("Uploading files to S3 failed.");
                logger.info("Converting {} to {}.", e, dataAccessException);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} throwed exception : {}.", jp.getSignature(), dataAccessException);
            }
            throw dataAccessException;
        }
    }

    /**
     * 
     * S3 삭제 기능을 담당하는 DAO 메서드에 적용되는 로깅 애스팩트.
     * 
     * @throws DataAccessException
     *             AWS S3 연산 중 {@link AmazonClientException} 타입 예외가 발생할 경우 이를 변환하여
     *             {@link DataAccessException}이 던져진다.
     * 
     */
    @Around("execution(* com.jisang.persistence.S3MultipartDAO.delete(..))")
    public Object s3DeleteLog(ProceedingJoinPoint jp) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting method {}#{}.", jp.getTarget(), jp.getSignature().toShortString());
        }
        if (logger.isInfoEnabled()) {
            logger.info("Starting to delete S3 files.");
        }
        try {
            Object result = jp.proceed();

            if (logger.isInfoEnabled()) {
                logger.info("Deleting S3 files suceeded.");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} finished with returnning : {}.", jp.getSignature(), result);
            }
            return result;
        } catch (AmazonClientException e) {
            String exceptionMessage = null;

            if (e instanceof AmazonServiceException) {
                AmazonServiceException ex = (AmazonServiceException) e;
                exceptionMessage = ex.getErrorCode() + "-" + ex.getErrorMessage() + " by" + ex.getErrorType()
                        + "'s fault.";
            } else {
                exceptionMessage = e.getMessage();
            }

            DataAccessException dataAccessException = new DataAccessException(exceptionMessage, e) {
                private static final long serialVersionUID = 3920413097909327731L;
            };

            if (logger.isInfoEnabled()) {
                logger.info("Deleting S3 files failed.");
                logger.info("Converting {} to {}.", e, dataAccessException);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Method #{} throwed exception : {}.", jp.getSignature(), dataAccessException);
            }
            throw dataAccessException;
        }
    }
}
