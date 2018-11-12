package com.jisang.web;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jisang.domain.HashTag;
import com.jisang.dto.ErrorDTO;
import com.jisang.dto.SearchCriteria;
import com.jisang.dto.SearchKeywordPreviewDTO;
import com.jisang.persistence.HashTagDAO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * 
 * 검색창에 미리보기로 제안할 키워드 (현재 구현은 해시태그 검색만 구현되었으나 후에 상품 이름 검색을 위한 키워드 미리보기 기능도 추가될 수 있다.)를 제공한다.
 * 간단히 데이터 전달 역할만 수행하기 때문에 특별히 서비스 오브젝트를 호출하지 않고 바로 DAO에 접근 후 응답하는 방식으로 구현하기로 했다.
 * 
 * @author leeseunghyun
 *
 */
@RestController
@Api(description = "Keyword preview search API")
public class SearchKeywordPreviewController {
	
	private Logger logger = LoggerFactory.getLogger(SearchKeywordPreviewController.class);

	@Autowired
	private HashTagDAO hashTagRepo;
	

	/**
	 * 	현재 구현은 해시태그 키워드 미리보기 검색만 구현되어 있으나 후에 상품 이름 검색 추가할 예정.
	 */
	@ApiOperation(value = "검색창 미리보기 키워드 검색", response = SearchKeywordPreviewDTO.class)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class)})
	@GetMapping("/search-keyword")
	public ResponseEntity<SearchKeywordPreviewDTO> getKeywordPreview(@Validated SearchCriteria searchCriteria){
		
		SearchKeywordPreviewDTO keywordPreviewDTO = searchHashTagKeyword(searchCriteria.getKeyword());

		return new ResponseEntity<>(keywordPreviewDTO, HttpStatus.OK);
	}
	
	private SearchKeywordPreviewDTO searchHashTagKeyword(String hashTagKeyword){
		logger.debug("Starting to search hashtag keyword for preview.");
		
		List<HashTag> hashTagList = hashTagRepo.searchByContent(hashTagKeyword);
		
		if(hashTagList.isEmpty()) {
			return null;
		}
		
		SearchKeywordPreviewDTO keywordPreview = new SearchKeywordPreviewDTO();
		
		hashTagList.stream().forEachOrdered(hashtag -> keywordPreview.addKeyword(hashtag.getContent()));
		
		logger.debug("Searching hashtags using {} suceeded.", hashTagRepo);
		
		return keywordPreview;
	}
	
}
