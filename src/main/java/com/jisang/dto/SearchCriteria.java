package com.jisang.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;

import com.jisang.domain.Product;
import com.jisang.web.SearchKeywordPreviewController;
import com.jisang.web.product.ProductController;
import com.jisang.service.product.ProductService;

/**
 * 
 * 현재는 해시태그를 기반으로 한 검색어 미리보기, 상품 검색만 존재하여 {@code keyword} 프로퍼티만 존재하나 후에 상품 이름 기반
 * 검색어 미리보기, 상품 검색이 추가되면 {@code searchType} 프로퍼티를 추가 정의할 예정이다.
 * 
 * 해시태그 및 상품 이름(현재는 구현 안됨)을 통한 상품 목록 검색은 {@link Product} 도메인 관련 컴포넌트
 * {@link ProductController}, {@link ProductService} 등에서 처리하며 키워드 미리보기와 같은 기능은
 * {@link SearchKeywordPreviewController}에서 처리한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class SearchCriteria {

    @ApiModelProperty(notes = "검색 키워드.", name = "keyword"
                    , value = "해시태그 검색 보기의 경우 검색할 해시태그를 입력한다. "
                            + "후에 추가 될 상품 이름 검색 기능의 경우에는 검색할 상품 이름을 지정하면 된다.")
    @NotBlank(message = "검색 키워드를 입력하세요.")
    private String keyword;

    // @Pattern(regexp = "^(hashtag|productName)$", message = "미리보기 타입 값이 올바르지
    // 않습니다.")
    // String searchType;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[keyword=" + keyword + "]";
    }

}
