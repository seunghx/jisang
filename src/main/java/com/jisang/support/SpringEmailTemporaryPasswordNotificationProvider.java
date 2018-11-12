package com.jisang.support;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;


/**
 * 
 * 임시 비밀 번호 전송의 역할을 수행하는 {@link TemporaryPasswordNotificationProvider} 구현으로 이 클래스는 이메일을 통해 
 * 임시 비밀 번호를 전송한다.
 * 
 * @author leeseunghyun
 *
 */
@Component("email")
public class SpringEmailTemporaryPasswordNotificationProvider implements TemporaryPasswordNotificationProvider {

	private final Logger logger = LoggerFactory.getLogger(SpringEmailTemporaryPasswordNotificationProvider.class);
	
	@Autowired
	private SpringTemplateEngine templateEngine;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private ResourceLoader resourceLoader;

	@Value("${thymeleaf.template.temporary-password.password-key}")
	private String passwordKey;
	@Value("${thymeleaf.template.temporary-password.img-cid-key}")
	private String imgCidKey;
	@Value("${thymeleaf.template.temporary-password.img-cid-value}")
	private String imgCidValue;
	@Value("${thymeleaf.template.temporary-password.representing-image.servlet-context-location}")
	private String representingImgLocation;
	@Value("${thymeleaf.template.temporary-password.template-location}")
	String templateLocation;
	@Value("${spring.mail.default-sender-id}")
	private String mailFromId;


	@Override
	public void sendTemporaryPassword(String email, String temporaryPassword) {
		if(StringUtils.isEmpty(email)){
			logger.warn("Argument email is empty. Checking code required.");
			throw new IllegalArgumentException("Argument destination is empty.");
		}
		if(StringUtils.isEmpty(temporaryPassword)) {
			logger.warn("Argument temporaryPassword is empty. Checking code required.");
			throw new IllegalArgumentException("Argument temporaryPassword is empty.");
		}
		
		
		try {			
			String htmlMessage = getMailMessage(temporaryPassword);
			
			MimeMessage message = mailSender.createMimeMessage();
			
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(mailFromId);
			helper.setTo(email);
			helper.setText(htmlMessage, true);
			helper.addInline(imgCidValue, resourceLoader.getResource(representingImgLocation));	
			
			mailSender.send(message);
		}catch(MessagingException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Converting {} to MailNotificationFailedException...", e.toString());
			}
			throw new MailNotificationFailedException(e.getMessage(), e);
		}	
	}
	
	
	private String getMailMessage(String temporaryPassword) {
		
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(imgCidKey, imgCidValue);
		parameterMap.put(passwordKey, temporaryPassword);
		
		Context context = new Context();
	    context.setVariables(parameterMap);
	    
		return templateEngine.process(templateLocation, context);
	}
}

