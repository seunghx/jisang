package com.jisang.service.product;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;



/**
 * 
 * {@link ProductService} 구현 클래스에 대한 애스팩트를 정의한 클래스.
 * 
 * 
 * 현재 구현은 {@link ProductService} 구현 클래스의 메서드의 시작과 끝을 알리는 로깅만이 정의되어 있다. 
 * 각각의 서비스 오브젝트의 메서드는 각각의 비즈니스 로직을 의미하므로 비즈니스 로직의 시작과 끝을 알린다는 의미로 info 레벨의 로깅을 수행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Aspect
@Component
@Order(2)
public class ProductServiceAspect {
	
	private final Logger logger = LoggerFactory.getLogger(ProductServiceAspect.class);
	
	@Around("execution(* com.jisang.service.product.*.findProductForShopping(..))")
	public Object findProductForShoppingLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to find product info for shopping view by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Finding product info for shopping succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Finding product info for shopping failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.findProductList(..))")
	public Object findProductListLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to find product list by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Finding product list succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Finding product list failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.registerProduct(..))")
	public Object registerProductLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to register product info by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Registering product info succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Registering product info failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.modifyProduct(..))")
	public Object modifyProductLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to modify product info by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Modifying product info succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Modifying product info failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.findProductForModifying(..))")
	public Object findProductForModifyLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to find product info for modifying by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Finding product info for modifying succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Finding product info for Modifying failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.findProductListForManagement(..))")
	public Object findProductListForManagementLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to find product list for management by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Finding product list for management succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Finding product list for management failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.findProductListByDate(..))")
	public Object findProductListByDateLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to find grouped by date product list by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Finding grouped by date product list for management succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Finding grouped by date product list for management failed.");
			}
			throw e;
		}
	}
	
	@Around("execution(* com.jisang.service.product.*.deleteProduct(..))")
	public Object deleteProductLog(ProceedingJoinPoint jp) throws Throwable {
		
		if(logger.isInfoEnabled()) {
			logger.info("Starting to delete product info by {}", jp.getTarget());
		}
		
		try {
			Object result = jp.proceed();

			if(logger.isInfoEnabled()){
				logger.info("Deleting product info succeeded");
			}
			
			return result;
		}catch(Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Deleting product info failed.");
			}
			throw e;
		}
	}

}
