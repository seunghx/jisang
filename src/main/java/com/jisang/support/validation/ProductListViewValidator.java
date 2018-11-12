package com.jisang.support.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;

import com.jisang.dto.product.ProductListViewConfigData;

import static com.jisang.config.code.CodeBook.ProductListViewType.BEST;
import static com.jisang.config.code.CodeBook.ProductListViewType.PAGE;
import static com.jisang.config.code.CodeBook.ProductListViewType.RECOMMENDED;
import static com.jisang.dto.product.ProductListViewConfigData.VIEW_TYPE_PROPERTY_NAME;
import static com.jisang.dto.product.ProductListViewConfigData.BEST_VIEW_CNT_PROPERTY_NAME;
import static com.jisang.dto.product.ProductListViewConfigData.PAGE_INDEX_PROPERTY_NAME;
import static com.jisang.dto.product.ProductListViewConfigData.PER_PAGE_CNT_PROPERTY_NAME;
import static com.jisang.dto.product.ProductListViewConfigData.RECOMMENDED_VIEW_CNT_PROPERTY_NAME;



/**
 * 
 * 지상 어플리케이션에는 현재 베스트 상품 목록 화면, 추천 상품 목록 화면, 일반(페이지네이션) 목록 화면 등 여러 타입의 상품 목록 화면이 존재한다. 또한 
 * 특정 마켓의 상품 목록이냐 특정 지하 상가 전체의 상품 목록이냐에 따르는 구분도 존재한다. 그리고 상품 카테고리에 따라 화면에 전달 될 상품 목록이 달라야 한다. 
 * 
 * 와이어 프레임의 마켓 상품 목록 화면에는 추천 상품 리스트와 일반 리스트(상품 목록이 많아질 것을 고려하여 페이지네이션으로 구현하였음. 와이어프레임처럼 단일 페이지에 출력되게 할 경우
 * perPageCnt를 )의 두 타입의 상품 목록이 존재한다. 그러나 상품 목록 화면 타입은 후에 바뀔 수 있기 때문에 
 * 마켓 상품 목록 화면 요청에 대한 응답 처리를 앞서 말한 두 타입의 상품 목록 화면에 대한 처리만 가능하도록구현할 경우 화면이 변경될 때마다 서버 코드 또한 변경되어야 한다.
 * (물론 화면이 변경되면 데이터를 응답해야하는 서버 코드의 변경이 동반되겠으나 그 범위가 작아진다는 뜻이다.
 * 예를 들어, 기존 베스트 상품 목록 화면이 추천 상품 목록 화면으로 바뀔 경우에는 현재 지상 어플리케이션 서버 구현은 변경될 필요가 없다. 그러나 다른 예를 들어, 상품 목록 화면에
 * 현재 지상 어플리케이션의 와이어프레임과 같이 상품 사진,이름,가격만 출력되는 것이 아니라 또 다른 정보(예를 들어 해시태그)가 추가되어야 한다고 할때는 당연히 서버 코드가 변경되어야 
 * 할 것이다. 응답용 DTO 클래스에 프로퍼티가 추가되는 등. 위의 서버 코드 변경의 범위가 작아진다는 의미는 앞서 말한 두 가지의 예 중 전자의 경우와 같이 화면 타입이 변경될 때에는 
 * 서버 코드가 변경되지않으므로(않게 구현하였으므로) 변경의 범위가 작아진다는 뜻이다.) 
 * 
 * 또한 추천 상품 목록 화면은 마켓 상품 목록 화면 외에 상품 검색 결과 화면에도 존재한다. 그리고 추천 상품 목록 화면은 후에 지하상가 별 상품 목록
 * 화면에도 충분히 적용될 수 있는 부분이다. 이로부터 market을 기준으로 상품을 가져오든 아니면 지하상가 코드를 기준으로 상품을 가져오든 특정 화면 구성 요청에 대한 응답을 일관되게
 * 적용하고 싶었다. 
 * 
 * 이를 막기 위해 {@link ProductListViewCriteria}와 계승 클래스들을 정의하였고 이들 클래스의 오브젝트가 필요로 하는 정보를 
 * 담은(클라이언트로부터 전달 받은 정보를 담음) {@link ProductListViewConfigData}에 대한 bean validation을 수행할 필요가 있었다.
 * 여러 타입의 상품 목록 화면에 대한 정보가 {@link ProductListViewConfigData}라는 하나의 클래스 오브젝트에 바인딩이 되기 때문에 바인딩 된 오브젝트의 프로퍼티들에 대한
 * 연관 관계를 검사할 필요가 있다. 아래 클래스는 이런 프로퍼티 간의 연관관계 검증을 수행한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class ProductListViewValidator implements 
											ConstraintValidator<ProductListViewConstraint, ProductListViewConfigData>{
	
	private final Logger logger = LoggerFactory.getLogger(ProductListViewValidator.class);
		
	private String viewType;
	private List<String> viewTypeProperties;
	
	public void initialize(ProductListViewConstraint constraintAnnotation) {
		this.viewType = constraintAnnotation.viewType();
	    this.viewTypeProperties = Arrays.asList(constraintAnnotation.viewTypeArguments());
	}
	
	/**
	 * {@link ProductListViewConfigData}에 대한 bean validation을 수행한다. 
	 * 상품 목록 화면 타입 정보에 대한 검사와 각 상품 목록 화면 타입에 대한 연관 프로퍼티의 검사를 수행한다. 
	 */
	@Override
	public boolean isValid(ProductListViewConfigData criteria, ConstraintValidatorContext context) {
		logger.debug("Starting to validate {}", criteria);
		
		context.disableDefaultConstraintViolation();

		BeanWrapperImpl bean = new BeanWrapperImpl(criteria);
		
		String viewTypeValue = (String)bean.getPropertyValue(viewType);
		Map<String, Object> viewTypeArgValues = new HashMap<>();
		
		viewTypeProperties.stream()
						  .forEach(property -> viewTypeArgValues.put(property, bean.getPropertyValue(property)));
		
		if(BEST.equalsByCode(viewTypeValue)) {
			logger.debug("Validating {} about viewType value {}", criteria, viewTypeValue);
			
			return bestViewValidation(viewTypeArgValues, criteria, context);
		}else if(PAGE.equalsByCode(viewTypeValue)) {
			logger.debug("Validating {} about viewType value {}", criteria, viewTypeValue);
			
			return pageViewValidation(viewTypeArgValues, criteria, context);
		}else if(RECOMMENDED.equalsByCode(viewTypeValue)) {
			logger.debug("Validating {} about viewType value {}", criteria, viewTypeValue);
			
			return recommendedViewValidation(viewTypeArgValues, criteria, context);
		}else {
			logger.info("Validating {} failed due to invalid viewType value : {}", criteria, viewTypeValue);
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.viewType.invalid}")
					   								.addPropertyNode(VIEW_TYPE_PROPERTY_NAME).addConstraintViolation();
			return false;
		}
	}
	
	
	private boolean bestViewValidation(Map<String, Object> viewTypeArgValues, ProductListViewConfigData criteria
									 									    , ConstraintValidatorContext context) {
		Integer bestViewCnt = (Integer)viewTypeArgValues.get(BEST_VIEW_CNT_PROPERTY_NAME);
		
		if(bestViewCnt == null) {
			logger.info("Validating {} failed due to missing property {}", criteria, BEST_VIEW_CNT_PROPERTY_NAME);
			context.buildConstraintViolationWithTemplate("${ProductListViewConstraint.best.association.invalid}")
			 								       .addPropertyNode(BEST_VIEW_CNT_PROPERTY_NAME).addConstraintViolation();
			return false;
		}else if(bestViewCnt <= 0) {
			logger.info("Validating {} failed due to invalid property value.", criteria);
			logger.info("Invalid value of property name {} : {}", BEST_VIEW_CNT_PROPERTY_NAME, bestViewCnt);
			
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.invalidPropertyValue.zeroAndNegative}")
		       									   .addPropertyNode(BEST_VIEW_CNT_PROPERTY_NAME).addConstraintViolation();
			return false;
		}
		
		logger.info("Validating {} succeeded.", criteria);
		return true;
	}
	
	
	private boolean pageViewValidation(Map<String, Object> viewTypeArgValues, ProductListViewConfigData criteria
			    														    , ConstraintValidatorContext context) {
		// nullable
		Integer perPageCnt = (Integer)viewTypeArgValues.get(PER_PAGE_CNT_PROPERTY_NAME);
		
		
		if(perPageCnt != null && perPageCnt <= 0) {
			logger.info("Validating {} failed due to invalid property value.", criteria);
			logger.info("Invalid value of property name {} : {}", PER_PAGE_CNT_PROPERTY_NAME, perPageCnt);
			
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.invalidPropertyValue.zeroAndNegative}")
		       									   .addPropertyNode(PER_PAGE_CNT_PROPERTY_NAME).addConstraintViolation();
			return false;
		}
		
		Integer pageIndex = (Integer)viewTypeArgValues.get(PAGE_INDEX_PROPERTY_NAME);
		
		if(pageIndex == null) {
			logger.info("Validating {} failed due to missing properties {}", criteria, PAGE_INDEX_PROPERTY_NAME);
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.page.association.invalid}")
			 										  .addPropertyNode(PAGE_INDEX_PROPERTY_NAME).addConstraintViolation();
			return false;
		}else if(pageIndex <= 0) {
			logger.info("Validating {} failed due to invalid property value.", criteria);
			logger.info("Invalid value of property name {} : {}", PAGE_INDEX_PROPERTY_NAME, pageIndex);
			
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.invalidPropertyValue.zeroAndNegative}")
		       									   .addPropertyNode(PAGE_INDEX_PROPERTY_NAME).addConstraintViolation();
			return false;
		}
		
		logger.info("Validating {} succeeded.", criteria);
		return true;
	}
	
	private boolean recommendedViewValidation(Map<String, Object> viewTypeArgValues, ProductListViewConfigData criteria
		    																       , ConstraintValidatorContext context) {
		
		Integer recommendedViewCnt = (Integer)viewTypeArgValues.get(RECOMMENDED_VIEW_CNT_PROPERTY_NAME);
		
		if(recommendedViewCnt == null) {
			logger.info("Validating {} failed due to missing property {}", criteria, RECOMMENDED_VIEW_CNT_PROPERTY_NAME);
			
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.recommended.association.invalid}")
											 .addPropertyNode(RECOMMENDED_VIEW_CNT_PROPERTY_NAME).addConstraintViolation();
			return false;
		}else if(recommendedViewCnt <= 0) {
			logger.info("Validating {} failed due to invalid property value.", criteria);
			logger.info("Invalid value of property name {} : {}", RECOMMENDED_VIEW_CNT_PROPERTY_NAME, recommendedViewCnt);
			
			context.buildConstraintViolationWithTemplate("{ProductListViewConstraint.invalidPropertyValue.zeroAndNegative}")
		       									   .addPropertyNode(RECOMMENDED_VIEW_CNT_PROPERTY_NAME).addConstraintViolation();
			return false;
		}
		
		logger.info("Validating {} succeeded.", criteria);
		return true;
	}
}
