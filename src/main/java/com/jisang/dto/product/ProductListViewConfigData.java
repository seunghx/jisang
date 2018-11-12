package com.jisang.dto.product;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.jisang.dto.SearchCriteria;
import com.jisang.support.validation.ProductListViewConstraint;
import com.jisang.support.validation.ProductListViewPropertyAssociation;
import com.jisang.support.validation.ProductListViewValidator;

import io.swagger.annotations.ApiModelProperty;



/**
 * 
 * 지상 어플리케이션 와이어 프레임의 상품 목록 보기 화면을 보면 크게 세 가지 타입의 상품 목록이 존재한다. 상품(카테고리)별 상품 목록 보기 화면의 베스트 상품 목록, 마켓 상품 목록
 * 화면의 마켓 추천 상품 목록(워크 플로우상에는 best라고 표시되어 있으나 이 부분은 실제로는 마켓 관리자의 추천 상품 목록이다.) 그리고 마지막으로 단순 상품 목록이다.
 * 상품 목록 화면이야 쉽게 변할 수 있는 요소라고 생각하였다. 즉, 마켓 상품 목록 화면의 상단이 (현재의 마켓 관리자 추천 상품 목록에서) 해당 마켓의 베스트 상품 목록으로 변할 수
 * 있다는 뜻이다. 이런 변화에 어느 정도 쉽게 대응하도록 하고 싶어 하나의 요청 URL로 여러 화면 타입에 대한 처리를 가능하게 하고 싶었다. 그렇게 이 클래스를 만들었다.
 * 
 * 
 * 상품 목록 보기 화면은 GET 메서드를 통해 동작한다. GET 메서드 호출로 클라이언트에서 서버에 부가적인 정보를 전달하려면 쿼리 스트링을 사용해야 할 것이다.  
 * 쿼리 스트링 방식을 이용해 여러 가지 뷰 타입 정보를 포함하는 criteria 클래스를 구성하려다 보니 서로 연관이 없는 프로퍼티들이 함께 존재하게 되었다. 
 * (이 클래스의 프로퍼티 {@code bestViewCnt}는 {@code pageIndex - 단순 상품 목록에 사용된다. -> 사실 와이어 프레임 상에는 페이지네이션은 없으나 상품 목록이 많아질 경우 
 * 페이지네이션은 필요할 것이다.}는 서로 전혀 연관이 없다.) 이러다보니 오동작을 피하기 위해 프로퍼티끼리의 연관 관계에 대한 유효성 검증이 필요하였다. (예를 들어, 
 * {@code viewType}이 "best" 일 때 {@code bestViewCnt}프로퍼티에 해당하는 파라미터가 같이 포함되었는지에 대한 검사를 한다.) 이를 위해 아래의 클래스 레벨 애노테이션
 * {@link ProductListViewConstraint}를 정의하여 선언하였다.
 * 
 * 
 * 이 클래스에는 {@code VIEW_TYPE_PROPERTY_NAME, CATEGORY_PROPERTY_NAME} 등과 같은 각 프로퍼티의 이름을 값으로 갖는 {@code static final String} 필드가
 * 존재한다. 이 필드들은 {@link ProductListViewValidator}에서 이 클래스의 오브젝트에 대한 클래스 레벨(프로퍼티 간의 연관관계 제약 검사.) bean validation을
 * 수행하다가 constraint violation이 발생할 경우 클라이언트에 전달할 메세지 생성을 위해 필요한 정보이다. {@link ProductListViewValidator}에서 "viewType"과 같은
 * 하드코딩을 피하기 위해 정의하였다. 이런 프로퍼티 명이 사용되는 곳은 {@link ProductListViewValidator}이지만 프로퍼티 명이 변경되거나 할 때 실수로 이들 프로퍼티 명을 나타내는
 * 필드가 변경되지 않을 경우를 그나마 방지하고자 해당 프로퍼티 명 필드들을 이 클래스에 정의하였다. 좋은 방법인지 잘 모르겠으나 현재의 나의 생각으로는 그나마 이렇게 하는 것이 괜찮아
 * 보인다.
 * 
 * 
 * @author - leeseunghyun
 * 
 */
@ProductListViewConstraint(viewType = "viewType", viewTypeArguments = {"bestViewCnt", "pageIndex", "perPageCnt", "recommendedViewCnt"}
						 , groups = {ProductListViewPropertyAssociation.class}, message = "뷰 타입 파라미터의 연관 관계가 올바르지 않습니다.")
public class ProductListViewConfigData {
		
	@ApiModelProperty(notes = "상품 목록 화면 타입 코드. 코드북에 지정된 productViewType 코드를 사용해야만 한다.", name = "viewType", required = true
					, allowableValues = "best, page, recommended")
	@NotBlank(message ="뷰 타입 정보가 올바르지 않습니다.")
	private String viewType;
	public static final String VIEW_TYPE_PROPERTY_NAME = "viewType";


	@ApiModelProperty(notes = "상품 카테고리 코드.", name = "category", required = true
					, value = "코드북에 지정된 productCategory 코드를 사용해야만 하며 전체 카테고리의 상품을 보기 원할 경우 productCategory.ALL 코드를 입력해야 한다."
					, example = "아우터 : '31'")
	@NotBlank(message = "상품 카테고리 코드를 입력하세요.")
	@Pattern(message = "상품 카테고리 정보가 올바르지 않습니다." , regexp="^3[0-8]$")
	private String category;
	public static final String CATEGORY_PROPERTY_NAME = "category";
		

	@ApiModelProperty(notes = "화면에 표시할 베스트 상품 개수.", name = "bestViewCnt" 
					, value = "viewType 을 productViewTeype.BEST로 하였을 경우 해당 프로퍼티를 꼭 지정(양수)해야 한다. 지정하지 않을 경우 400 - Bad Request 응답 반환.")
	private Integer bestViewCnt;
	public static final String BEST_VIEW_CNT_PROPERTY_NAME = "bestViewCnt";
	
	@ApiModelProperty(notes = "페이지 번호.", name = "pageIndex"
			, value = "viewType 을 productViewTeype.PAGE로 하였을 경우 해당 프로퍼티를 꼭 지정(양수)해야 한다. 지정하지 않을 경우 400 - Bad Request 응답 반환.")
	private Integer pageIndex;
	public static final String PAGE_INDEX_PROPERTY_NAME = "pageIndex";
	
	@ApiModelProperty(notes = "페이지 당 표시될 상품 수.", name = "perPageCnt"
			, value = "viewType 을 productViewTeype.PAGE로 하였을 경우 양수로 지정해야 한다 그렇지 않을 경우 400 - Bad Request 응답 반환. 예외로 현재의 지상 "
					+ "어플리케이션의 와이어프레임과 같이 페이지네이션이 없는 일반 상품 목록 화면 구성이 필요한 경우 이 프로퍼티를 전달하지 말아야 한다.")
	private Integer perPageCnt;
	public static final String PER_PAGE_CNT_PROPERTY_NAME = "perPageCnt";

	@ApiModelProperty(notes = "화면에 표시할 추천 상품 수.", name = "recommendedViewCnt"
			, value = "viewType 을 productViewTeype.RECOMMENDED로 하였을 경우 해당 프로퍼티를 꼭 지정(양수)해야 한다. 지정하지 않을 경우 400 - Bad Request 응답 반환.")	
	private Integer recommendedViewCnt;
	public static final String RECOMMENDED_VIEW_CNT_PROPERTY_NAME = "recommendedViewCnt";

	@ApiModelProperty(notes = "상품 검색 criteria.", name = "searchCriteria")
	private SearchCriteria searchCriteria = new SearchCriteria();

	
	/**
	 * 아래의 두 프로퍼티는 url {@code /market/{marketId}/products 또는 /mall/{mallLocation}/products}에 해당하는 핸들러 메서드에서 값을 설정해 줄 것이다.
	 */
	@ApiModelProperty(hidden = true)
	private Integer marketId;
	@ApiModelProperty(hidden = true)
	private String mallLocation;
	
	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public Integer getBestViewCnt() {
		return bestViewCnt;
	}

	public void setBestViewCnt(Integer bestViewCnt) {
		this.bestViewCnt = bestViewCnt;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}
	
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPerPageCnt() {
		return perPageCnt;
	}

	public void setPerPageCnt(Integer perPageCnt) {

		this.perPageCnt = perPageCnt;
	}

	public Integer getRecommendedViewCnt() {
		return recommendedViewCnt;
	}

	public void setRecommendedViewCnt(Integer recommendedViewCnt) {

		this.recommendedViewCnt = recommendedViewCnt;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getMarketId() {
		return marketId;
	}

	public void setMarketId(Integer marketId) {
		this.marketId = marketId;
	}

	public String getMallLocation() {
		return mallLocation;
	}

	public void setMallLocation(String mallLocation) {
		this.mallLocation = mallLocation;
	}
	
	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(SearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
	
	@Override
	public String toString() {
		return "ProductListViewConfigData [viewType=" + viewType + ", category=" + category + ", bestViewCnt="
				+ bestViewCnt + ", pageIndex=" + pageIndex + ", perPageCnt=" + perPageCnt + ", recommendedViewCnt="
				+ recommendedViewCnt + ", marketId=" + marketId + ", mallLocation=" + mallLocation + ", searchCriteria="
				+ searchCriteria + "]";
	}

}
