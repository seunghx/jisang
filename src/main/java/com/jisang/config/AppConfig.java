package com.jisang.config;


import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.validation.Validator;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.Formatter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.SetSMSAttributesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jisang.persistence.MybatisMapper;
import com.jisang.support.conversion.KoreaPhoneNumberFormatProvider;
import com.jisang.support.conversion.PhoneNumberFormatProvider;
import com.jisang.support.conversion.PhoneNumberFormatter;
import com.jisang.support.validation.Korea;
import com.jisang.support.validation.LocaleBasedValidationGroups;



@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@MapperScan(basePackages = "com.jisang.persistence", markerInterface = MybatisMapper.class)
@Import({DataSourceConfig.class, SecurityConfig.class, ModelMapperConfig.class, ViewConfig.class})
@ImportResource("classpath:awsConfig.xml")
@ComponentScan(basePackages={ "com.jisang.service", "com.jisang.validation", "com.jisang.support", "com.jisang.domain"
							, "com.jisang.persistence", "com.jisang.aop", "com.jisang.config.code"})
@PropertySource("classpath:application.properties")
@EnableRetry
@EnableScheduling
public class AppConfig {
	
	/*
	 * AmazonSNS configuration property names.
	 */
	private static final String MESSAGE_FILE_NAME = "messages";
	private static final String SMS_SENDER_ID_KEY = "DefaultSenderID";
	private static final String SMS_MONTHLY_PRICE_LIMIT_KEY = "MonthlySpendLimit";
	private static final String SMS_TYPE_KEY = "DefaultSMSType";
	
	/*
	 * java mail configuration property names.
	 */
	private static final String JAVA_MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
	

	@Autowired
	private DataSource dataSource;
	
	@Value("${spring.mail.host}")
	String mailHost;
	@Value("${spring.mail.username}")
	String mailUserName;
	@Value("${spring.mail.password}")
	String mailPassword;
	@Value("${spring.mail.properties.mail.transport.protocol}")
	String mailProtocol;
	@Value("${spring.mail.properties.mail.smtp.port}")
	int mailPort;
	@Value("${spring.mail.properties.mail.smtp.auth}")
	boolean mailSmtpAuth;
	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	boolean mailTlsEnabled;
	@Value("${spring.mail.properties.mail.smtp.starttls.required}")
	boolean mailTlsRequired;
			

	@Value("${aws.access-key.id}") 
	private String awsAccessKeyId;
	@Value("${aws.secret.access-key}")
	private String awsSecretAccessKey;
	@Value("${aws.sns.region}")
	private String awsSnsRegion;
	@Value("${aws.sns.sms.sender-id}")
	private String defaultSmsSenderId;
	@Value("${aws.sns.sms.max-price}")
	private String defaultSmsMaxPrice;
	@Value("${aws.sns.sms.sms-type}")
	private String defaultSmsType;

	
	@Bean
	public List<String> imageTrashCan() {
		return Collections.synchronizedList(new ArrayList<String>());
	}
	
	@Bean
	public List<Integer> hashtagTrashCan() {
		return Collections.synchronizedList(new ArrayList<Integer>());
	}
	
	@Bean
	public JavaMailSender getJavaMailSender() {
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(mailHost);
	    mailSender.setPort(mailPort);
	     
	    mailSender.setUsername(mailUserName);
	    mailSender.setPassword(mailPassword);
	    
	    Properties props = mailSender.getJavaMailProperties();
	    props.put(JAVA_MAIL_TRANSPORT_PROTOCOL, mailProtocol);
	    props.put(SMTP_AUTH, mailSmtpAuth);
	    props.put(SMTP_STARTTLS_ENABLE, mailTlsEnabled);
	    props.put(SMTP_STARTTLS_REQUIRED, mailTlsRequired);

	    return mailSender;
	}
	
	@Bean
	public AmazonSNS amazonSNS() {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
		
		AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
													.withRegion(Regions.fromName(awsSnsRegion))
													.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
													.build();
		
		SetSMSAttributesRequest setRequest = new SetSMSAttributesRequest()
													.addAttributesEntry(SMS_SENDER_ID_KEY, defaultSmsSenderId)
													.addAttributesEntry(SMS_MONTHLY_PRICE_LIMIT_KEY, defaultSmsMaxPrice)
													.addAttributesEntry(SMS_TYPE_KEY, defaultSmsType);
		
		snsClient.setSMSAttributes(setRequest);
		
		return snsClient;
	}
	
	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return Jackson2ObjectMapperBuilder.json()
										  .indentOutput(true)
										  .build();
	}

    @Bean
    public Validator validatorFactory() {
    	return new LocalValidatorFactoryBean();
    }
	
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
       return new PropertySourcesPlaceholderConfigurer();
    }
	
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames(MESSAGE_FILE_NAME);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
		return messageSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager dstm = new DataSourceTransactionManager(dataSource);
		dstm.setRollbackOnCommitFailure(true);
		return dstm;
	}
	
	@Bean
	public SqlSessionFactoryBean sqlSessionFactory() {
		SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setConfigLocation(new ClassPathResource("/mybatis-config.xml"));
		
		return sessionFactoryBean;
	}
	
	@Bean 
	public SqlSessionTemplate sqlSession() throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory().getObject());
	}
	
	@Bean
	public Map<Locale, Class<? extends LocaleBasedValidationGroups>> localeValidationGroupsMapper(){
		return Collections.unmodifiableMap(new HashMap<Locale, Class<? extends LocaleBasedValidationGroups>>() {
		
			private static final long serialVersionUID = -2497243059001490209L;

			{
				put(Locale.KOREA, Korea.class);
				put(Locale.KOREAN, Korea.class);
			}
		});
	}

	@Bean
	public Formatter<String> phoneNumberFormatter() {
		List<PhoneNumberFormatProvider> formatters = new ArrayList<PhoneNumberFormatProvider>() {
		
			private static final long serialVersionUID = -5379610941126668525L;

			{
				add(new KoreaPhoneNumberFormatProvider());
			}
		};
		
		return new PhoneNumberFormatter(formatters);
	}
	
	@Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }
	
}
