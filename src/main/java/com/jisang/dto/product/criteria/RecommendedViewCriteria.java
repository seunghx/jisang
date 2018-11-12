package com.jisang.dto.product.criteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 추천 상품 목록 화면 구성에 필요한 정보를 담은 {@link ProductListViewCriteria} 계승 클래스.
 *
 * @author leeseunghyun
 *
 */
public class RecommendedViewCriteria extends ProductListViewCriteria {
	
	private final int recommendedViewCnt;
	
	private RecommendedViewCriteria(RecommendedViewCriteriaBuilder builder) {
		super(builder);
		this.recommendedViewCnt = builder.recommendedViewCnt;
	}
	
	public int getRecommendedViewCnt() {
		return recommendedViewCnt;
	}

	public static class RecommendedViewCriteriaBuilder extends ProductListViewCriteriaBuilder {
		private static final Logger logger = LoggerFactory.getLogger(RecommendedViewCriteriaBuilder.class);
		private final int recommendedViewCnt;
		
		public RecommendedViewCriteriaBuilder(Integer marketId, String mallLocation, String category, int recommendedViewCnt) {
			super(marketId, mallLocation, category);
			this.recommendedViewCnt = recommendedViewCnt;
			
			if(this.recommendedViewCnt <= 0) {
				logger.error("Invalid argument recommendedViewCnt detected : {}. bestViewCnt must not be zero or negative value.", recommendedViewCnt);
				logger.error("Because arguent recommendedViewCnt passed to bean validation, It must be caused by illegal code. Checking illegal code required.");
				throw new IllegalStateException("Invalid argument recommendedViewCnt detected.");
			}
		}

		@Override
		public RecommendedViewCriteriaBuilder searchKeyword(String searchKeyword) {
			this.searchKeyword = searchKeyword;
			return this;
		}
		
		/**
		 * {@link RecommendedViewCriteria} 오브젝트를 생성하여 반환한다. 
		 */
		@Override
		public RecommendedViewCriteria build() {
			return new RecommendedViewCriteria(this);
		}

		
	}
	
	@Override
	public String toString() {
		return super.toString() + "[recommendedViewCnt=" + recommendedViewCnt + "]";
	}
}
