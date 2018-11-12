package com.jisang.support.validation;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * 
 * {@link com.jisang.dto.product.ProductListViewConfigData}에 대한 핸들러 메서드 파라미터로의
 * 바인딩 시 bean validation의 순서를 지정하기 위하여 아래 애노테이션을 정의하였다. 이 애노테이션에 따르면 프로퍼티 레벨에 먼저
 * validation 수행한 후에 클래스 레벨의 프로퍼티 연관 관계 validation이 수행된다.
 * 
 * @author leeseunghyun
 *
 */
@GroupSequence({ Default.class, ProductListViewPropertyAssociation.class })
public interface ProductListViewConstraintSequence {

}
