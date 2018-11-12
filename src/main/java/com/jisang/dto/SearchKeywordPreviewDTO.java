package com.jisang.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 검색 창에 표시될 검색 키워드 미리보기 요청에 대한 응답에 이용될 DTO 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class SearchKeywordPreviewDTO {
	
	private List<String> keywords = new ArrayList<>();

	public List<String> getKeyword() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	public void addKeyword(String keyword) {
		keywords.add(keyword);
	}

	@Override
	public String toString() {
		return getClass().getName() + "[keywords=" + keywords + "]";
	}
}
