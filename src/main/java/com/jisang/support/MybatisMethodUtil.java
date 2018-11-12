package com.jisang.support;

import static com.jisang.config.code.CodeBook.ProductCategory.ALL;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jisang.dto.product.ProductListViewConfigData;
import com.jisang.dto.product.criteria.ProductListViewCriteria;

import static com.jisang.config.code.CodeBook.MallLocation.ENTIRE_LOCATION;

/**
 * 
 * Mybatis Mapper.xml 파일에서 사용될 자바 메서드를 정의해둔 유틸리티 클래스이다.
 * 
 * @author leeseunghyun
 *
 */
public class MybatisMethodUtil {
	
	private static Logger logger = LoggerFactory.getLogger(MybatisMethodUtil.class);
	
	public static boolean isProductCategoryAll(String category) {
		return ALL.equalsByCode(category);
	}
	
	public static boolean isLocationEntire(String location) {
		return ENTIRE_LOCATION.equalsByCode(location);
	}
	
	public static boolean isSearchView(ProductListViewCriteria productCriteria) {
		return Optional.ofNullable(productCriteria)
					   .map(criteria -> !Objects.isNull(criteria.getSearchKeyword()))
					   .orElseThrow(() -> 
					   		new IllegalArgumentException("Null value productCriteria detected while trying to check search"
					   																+ "check wheter client requested search view or not"));
		
	}
	
	public static boolean isSearchView(ProductListViewConfigData productViewConfigData) {
		return Optional.ofNullable(productViewConfigData)
					   .filter(configData -> !Objects.isNull(configData.getSearchCriteria()))
					   .map(configData -> !Objects.isNull(configData.getSearchCriteria().getKeyword()))
					   .orElseThrow(() -> {
						   logger.info("Invalid value detected related to productViewConfigData : {}", productViewConfigData);
						   throw new IllegalArgumentException("Received invalid argument productViewConfigData detected while trying to "
						   															+ "check wheter client requested search view or not");
						   
					   });
	}

}
