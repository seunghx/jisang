package com.jisang.dto.product.criteria;


/** 
 * 
 * 지상 어플리케이션 와이어 프레임의 대부분을 차지하는 상품 목록 보기 화면에 대한 처리를 위해 필요한 DTO 클래스이다. 지상 어플리케이션에는 여러 유형의 상품 목록 보기 화면이 있다.
 * 크게 특정 마켓의 상품 목록, 지역별 상품 목록, 해시 태그 검색을 통한 상품 목록으로 나눌 수 있으며 이들 각각의 상품 목록은 best, MD's pick(recommended),
 * 일반 리스트(page)로 나뉜다. 현재 와이어 프레임 상의 화면에 따라 구현을 하다보면 후에 화면 변경이 있을 경우(예 : MD's pick 상품 목록을 best 상품 목록으로 변경)
 * 매번 코드를 변경해야 한다. 그래서 나는 이 클래스 {@link ProductListViewCriteria}를 정의하여 화면 변화에 조금이나마 유연성을 부여하기로 하였다. 위의 예와 같은 
 * 화면 변경이 필요할 경우 클라이언트에서는 원하는 화면 구성에 필요한 이 클래스의 프로퍼티에 해당하는 요청 변수(파라미터)만 변경하면 된다. 
 * 
 * 처음엔 다음과 같이 구현을 하려고 하였다. 특정 마켓의 상품 목록 화면에 대한 요청이 있을 때 DAO에서 도메인 오브젝트 {@link Market}을 반환케 하고 {@link Market} 
 * 오브젝트에 내부적으로 {@link Product}에 대한 리스트를 프로퍼티로 담게 하는 방법이다. 그러나 와이어 프레임을 보면 알 수 있듯 상품 목록 화면이 다양하기 때문에 
 * 이 방법만으로는 화면을 다 구성할 수 없다. 추가적으로 (현재 와이어 프레임의 마켓 상품목록 화면을 예로 들면) MD's pick 상품 목록(화면에는 best라고 표시되어 있으나 사실은
 * MD's pick이다.)이 더 필요하다. 
 * 
 * 위에 설명한 현재 구현과 같은 상품 목록 보기 방식은 변경에도 보다 유연하고 대부분의 경우의 상품 목록 보기 화면을 다 커버할 수 있다.
 * 
 * 
 * 구체적인 화면 타입에 필요한 정보는 서브 클래스에서 정의한다.
 * 
 * @author leeseunghyun
 *
 */
public class ProductListViewCriteria {
		
	private final Integer marketId;
	private final String mallLocation;
	private final String category;
	private final String searchKeyword;
	
	
	protected ProductListViewCriteria(ProductListViewCriteriaBuilder builder) {
		this.marketId = builder.marketId;
		this.mallLocation = builder.mallLocation;
		this.category = builder.category;
		this.searchKeyword = builder.searchKeyword;
	}
	
	public Integer getMarketId() {
		return marketId;
	}

	public String getMallLocation() {
		return mallLocation;
	}

	public String getCategory() {
		return category;
	}
	
	public String getSearchKeyword() {
		return searchKeyword;
	}

	public abstract static class ProductListViewCriteriaBuilder {
		
		private final Integer marketId;
		private final String mallLocation;
		private final String category;
		protected String searchKeyword;
		
		ProductListViewCriteriaBuilder(Integer marketId, String mallLocation, String category){
			this.marketId = marketId;
			this.mallLocation = mallLocation;
			this.category = category;
		}
				
		public abstract ProductListViewCriteria build();
		public abstract ProductListViewCriteriaBuilder searchKeyword(String searchKeyword);
	}

	@Override
	public String toString() {
		return getClass().getName() + "[marketId=" + marketId + ", mallLocation=" + mallLocation + ", category="
									+ category + ", searchKeyword=" + searchKeyword;
	}
	
	
}
