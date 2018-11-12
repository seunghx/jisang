package com.jisang.config;


import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;
import com.jisang.web.LoggingHandlerInterceptor;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableWebMvc
@EnableSwagger2
@ComponentScan(basePackages= "com.jisang.web")
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Value("${application.message.InternalServerError}")
	private String internalServerErrorMSG;
	@Value("${application.message.Notfound")
	private String notFoundMSG;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoggingHandlerInterceptor()).addPathPatterns("/**");
	}
	
	@Bean
    public Docket api() { 
		
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.any())                          
          .build()
          .apiInfo(apiInfo())
          .ignoredParameterTypes(UserID.class)
          .ignoredParameterTypes(Locale.class)
          .useDefaultResponseMessages(false)
          .globalResponseMessage(RequestMethod.GET
        		  			   , Collections.singletonList(new ResponseMessageBuilder().code(500)
        		  																	   .message(internalServerErrorMSG)
        		  																	   .build()))
          .globalResponseMessage(RequestMethod.POST
        		  			   , Collections.singletonList(new ResponseMessageBuilder().code(500)
        		  																	   .message(internalServerErrorMSG)
        		  																	   .build()))
          .globalResponseMessage(RequestMethod.PUT
	  			   			   , Collections.singletonList(new ResponseMessageBuilder().code(500)
	  																	   			   .message(internalServerErrorMSG)
	  																	   			   .build()))
          .globalResponseMessage(RequestMethod.DELETE
	  			   			   , Collections.singletonList(new ResponseMessageBuilder().code(500)
	  																	   			   .message(internalServerErrorMSG)
	  																	   			   .build()))
          .globalResponseMessage(RequestMethod.PATCH
	  			   			   , Collections.singletonList(new ResponseMessageBuilder().code(500)
	  																	   			   .message(internalServerErrorMSG)
	  																	   			   .build()))
          .globalResponseMessage(RequestMethod.GET
	  			   			   , Collections.singletonList(new ResponseMessageBuilder().code(404)
	  																	   			   .message(notFoundMSG)
	  																	   			   .build()))
          .globalResponseMessage(RequestMethod.POST
	  			    		   , Collections.singletonList(new ResponseMessageBuilder().code(404)
	  																	   		  	   .message(notFoundMSG)
	  																	   		  	   .build()))
          .globalResponseMessage(RequestMethod.PUT
		   			   		   , Collections.singletonList(new ResponseMessageBuilder().code(404)
																   			   		   .message(notFoundMSG)
																   			   		   .build()))
          .globalResponseMessage(RequestMethod.DELETE
		   			   		   , Collections.singletonList(new ResponseMessageBuilder().code(404)
																   			   		   .message(notFoundMSG)
																   			   		   .build()))
          .globalResponseMessage(RequestMethod.PATCH
		   			   		   , Collections.singletonList(new ResponseMessageBuilder().code(404)
																   			   		   .message(notFoundMSG)
																   			   		   .build()));
    }
	
	private ApiInfo apiInfo() {
	     return new ApiInfo(
	       "JISANG REST API", 
	       "지상 어플리케이션에서 지원하는 각 url에 대한 API 명세.", 
	       "JISANG API Version 1.0.0", 
	       "Terms of service", 
	       new Contact("Lee seunghyun", "", "seunghx@naver.com.com"), 
	       "Copyright ⓒ JISANG Corp. All Rights Reserved.", "JISANG license URL", Collections.emptyList());
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("swagger-ui.html")
	      .addResourceLocations("classpath:/META-INF/resources/");
	 
	    registry.addResourceHandler("/webjars/**")
	      .addResourceLocations("classpath:/META-INF/resources/webjars/");
	    
	    registry.addResourceHandler("/asset/**")
	    	.addResourceLocations("/WEB-INF/asset/");
	}
	
	 @Override
	 public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		 	argumentResolvers.add(new UserIDArgumentResolver());
	 }
 }
