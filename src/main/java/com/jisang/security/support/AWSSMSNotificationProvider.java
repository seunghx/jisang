package com.jisang.security.support;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * 
 * 사용자에게 SMS를 전송할 필요가 있을 때 해당 클래스의 메서드가 사용된다. 현재 구현에서는 security 단에서 사용자에게 SMS를
 * 전송할 일은 임시 비밀번호 발급 기능 밖에 없으므로
 * {@link AuthenticationNumberNotificationProvider}의
 * 메서드({@link #sendAuthenticationNumber}) 외에는 정의한 메서드가 따로 없다.
 *
 * 
 * @author leeseunghyun
 *
 */
@Component
public class AWSSMSNotificationProvider implements AuthenticationNumberNotificationProvider {
    private final Logger logger = LoggerFactory.getLogger(AWSSMSNotificationProvider.class);

    @Autowired
    private AmazonSNS snsClient;
    @Autowired
    private MessageSource msgSource;

    /**
     *
     * 인증 번호를 전송한다.
     * 
     * @param destination
     *            - SMS 수신자의 E.164 형식 핸드폰 번호.
     * @param authenticationNumber
     *            - SMS로 전달되는 인증번호.
     * 
     */
    @Override
    public void sendAuthenticationNumber(String destination, String authenticationNumber) {

        if (StringUtils.isEmpty(destination)) {
            logger.info("Argument destination is empty. destination : {}", destination);
            throw new InternalAuthenticationServiceException(
                    "Illegal argument detected. Argument destination must not be empty String.");
        }
        if (StringUtils.isEmpty(authenticationNumber)) {
            logger.info("Argument authenticationNumber is empty. authenticationNumber : {}", authenticationNumber);
            throw new InternalAuthenticationServiceException(
                    "Illegal argument detected. Argument authenticationNumber must not be empty String.");
        }

        String message = msgSource.getMessage("sms.authentication-number.message",
                new String[] { authenticationNumber }, Locale.getDefault());

        try {
            PublishResult result = snsClient
                    .publish(new PublishRequest().withMessage(message).withPhoneNumber(destination));

            logger.info("{} returned {}", snsClient, result);

        } catch (AmazonClientException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Converting {} to AuthenticationServiceException...", e.toString());
            }
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }
}
