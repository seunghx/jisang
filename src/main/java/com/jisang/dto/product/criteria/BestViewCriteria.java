package com.jisang.dto.product.criteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 베스트 상품 목록 화면 구성에 필요한 정보를 담은 {@link ProductListViewCriteria} 계승 클래스.
 * 
 * @author leeseunghyun
 *
 */
public class BestViewCriteria extends ProductListViewCriteria { 
	
	private final int bestViewCnt;

	private final double pastHitWeight;
	private final double currentHitWeight;
	
	
	private BestViewCriteria(BestViewCriteriaBuilder builder) {
		super(builder);
		this.bestViewCnt = builder.bestViewCnt;
		this.pastHitWeight = builder.pastHitWeight;
		this.currentHitWeight = builder.currentHitWeight;
	}
	
	public int getBestViewCnt() {
		return bestViewCnt;
	}
	
	public double getPastHitWeight() {
		return pastHitWeight;
	}

	public double getCurrentHitWeight() {
		return currentHitWeight;
	}

	/**
	 * {@link BestViewCriteriaBuilder}에 대한 빌더 클래스이다. 이 클래스의 생성자 및 수정자 메서드에서 전달된 인자에 대한 유효성 검사를 수행함을 알 수 있는데,
	 * 사실 이 클래스에 전달되는 값은 모두 컨틀롤러 단에서의 bean validation에 통과한 값들이다. 그러나 혹시 모를 코드의 변경으로 인한 잘못된 값의 전달에 따른 뒷단의 오동작을
	 * 방지하고자 검사를 하였다. 확실하게 안전함을 보장할 수 있기 때문이다. 그러나, 테스트에 거쳐 코드 문제가 없음을 알며 동시에 bean validation을 통과하였음까지 아는 상황에서
	 * 또 한번 검사하는 것은 불필요한 것은 아닌가란 생각도 들어 정답을 모르겠다. 
	 */
	public static class BestViewCriteriaBuilder extends ProductListViewCriteriaBuilder {
		
		private static final Logger logger = LoggerFactory.getLogger(BestViewCriteriaBuilder.class);
		
		private double DEFAULT_PAST_HIT_WEIGHT = 0.5;
		private double DEFAULT_CURRENT_HIT_WEIGHT = 1;
		
		private final int bestViewCnt;
		private double pastHitWeight = DEFAULT_PAST_HIT_WEIGHT;
		private double currentHitWeight = DEFAULT_CURRENT_HIT_WEIGHT;
		
		
		public BestViewCriteriaBuilder(Integer marketId, String mallLocation, String category, int bestViewCnt) {
			super(marketId, mallLocation, category);
			this.bestViewCnt = bestViewCnt;
			
			if(this.bestViewCnt < 0) {
				logger.error("Invalid argument bestViewCnt detected : {}. bestViewCnt must not be negative value.", bestViewCnt);
				logger.error("Because arguent bestViewCnt passed to bean validation, It must be caused by illegal code. Checking illegal code required.");
				
				throw new IllegalStateException("Invalid argument bestViewCnt detected.");
			}
		}

		public BestViewCriteriaBuilder pastHitWeight(double pastHitWeight) {
			this.pastHitWeight = pastHitWeight;
			return this;
		}

		public BestViewCriteriaBuilder currentHitWeight(double currentHitWeight) {
			this.currentHitWeight = currentHitWeight;
			return this;
		}

		@Override
		public BestViewCriteriaBuilder searchKeyword(String searchKeyword) {
			this.searchKeyword = searchKeyword;
			return this;
		}
		
		/**
		 * {@link BestViewCriteria} 오브젝트를 생성하여 반환한다. 
		 */
		@Override
		public BestViewCriteria build() {
			return new BestViewCriteria(this);
		}		
	}
	
	@Override
	public String toString() {
		return super.toString() + ", bestViewCnt="  + bestViewCnt + ", pastHitWeight=" + pastHitWeight 
							    + "currentHitWeight= " + currentHitWeight + "]";
	}
}
