package com.jisang.dto.product.criteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 페이지 형식의 상품 리스트 화면 구성에 사용될 {@link ProductListViewCriteria} 계승 클래스이다.
 * 
 * 현재 지상 어플리케이션의 와이어프레임을 보면 페이지네이션은 없음을 알 수 있으나 후의 화면 변경을 어느정도는 고려하여 워크 플로우 상의
 * 단순 상품 리스트 화면은 총 페이지 수 한 개 짜리의 페이지 뷰라고 정하였다. 상품 수가 많아지면 페이징이 불가피할 수 있는데 이럴 경우
 * 변경이 용이하다.
 * 
 * 이 클래스의 {@link displayPageCnt}필드는 화면에 표시될 페이지 인덱스의 수를 의미하는 필드이다. 그러나 아직
 * 페이지네이션이 어플리케이션 화면에 적용된 상태가 아닐 뿐더러 페이지네이션이 적용된다고 하여도 위의 필드를 이용해 페이지 번호를 나타낼
 * 것인지는 정해지지도 않았으므로(여기서 말하는 페이지란 전통적인 웹 게시판의 페이지네이션 화면에 사용되는 1 2 3 4 5 6 7 8 9
 * 10 >next> 와 같은 페이지 번호를 나타는 것을 말한다. displayPageCnt가 10일 경우.) 우선 필드만 정의 해두었다.
 * 후에 페이비 번호 표시 등의 기능이 필요할 경우에 기능을 추가하려고 하며 현재는 클라이언트에게 현재 페이지와 마지막 페이지만 전달한다.
 * 모바일에서는 이정도면 충분하다고 생각한다.
 * 
 * 이처럼 현재 존재하지도 않는 기능을 위해 프로퍼티를 정의해둔 이유는 필요에 의해서 라기보단 억지로 Builder 패턴을 적용해보려다 보니
 * 하게 된 끼워 맞추기에 가까운 것 같다. 필수 인자({@code totalProductCnt})와 선택적 인자(나머지 프로퍼티 - 선택적
 * 인자로 선택한 이유는 default 값을 지정할 수 있으므로 잘못 전달된 인자에 대해 오작동을 피할 수 있다. -> Builder의
 * {@code #build()} 메서드에서 유효성 검사를 하여 예외를 던지는 방법도 있겠으나 그렇게 하고 싶지 않아 default 값 지정
 * 방식을 선택했다.)가 적절히 나뉘어야 Builder 패턴을 쓴 이유가 성립되기 때문이다. 연습삼아 적용해본 것인 만큼 (억지
 * 적용을)그러려니 하려고 한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class PageViewCriteria extends ProductListViewCriteria {

    private final Integer pageIndex;
    private final Integer perPageCnt;

    private final Integer totalProductCnt;

    /** 현재 구현에서는 사용되지 않음. */
    // private final int displayPageCnt;

    private PageViewCriteria(PageViewCriteriaBuilder builder) {
        super(builder);
        this.pageIndex = builder.pageIndex;
        this.perPageCnt = builder.perPageCnt;
        this.totalProductCnt = builder.totalProductCnt;
        // this.displayPageCnt = builder.displayPageCnt;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPerPageCnt() {
        return perPageCnt;
    }

    public Integer getTotalProductCnt() {
        return totalProductCnt;
    }

    /*
     * public int getDisplayPageCnt() { return displayPageCnt; }
     */

    /**
     * 
     * @return 현재 page의 첫 번째 product의 index를 반환한다. 이 값은 SQL의 LIMIT x, y 에서의 x 값으로
     *         사용된다. {@code perPageCnt} 값이 null일 경우는 페이지네이션을 적용 안한 일반 상품 목록(현재의 지상
     *         어플리케이션 와이어프레임 상의 상품 목록 화면)에 대한 요청이 있을 경우인데, 이 경우는 LIMIT x, y와 같은 SQL
     *         쿼리가 필요 없고 또한 이 경우에는 이 메서드가 호출될 일이 없다.(ProductMapper.xml의
     *         'readListPage'를 id로 갖는 select 태그에서는 {@code perPageCnt}가 null이 아닐 경우에만
     *         LIMIT 구문을 쿼리에 추가한다.) 그래도 최소한의 안전 장치는 필요하다는 생각에 이 메서드에서는 if 절을 통한 null
     *         검사를 수행하여 오동작하지 않도록 한다.
     * 
     */
    public int getStartProductIdx() {
        if (perPageCnt == null) {
            throw new IllegalStateException(
                    "Illegal access detected. In case that perPageCnt is null, this method should not be called.");
        }
        return (pageIndex - 1) * perPageCnt;
    }

    /**
     * 
     * 클라이언트의 요청 정보에 따라 정해진 마지막 페이지의 인덱스를 반환한다. 현재의 지상 어플리케이션의 상품 목록 화면에는(와이어프레임
     * 상에는) 페이지네이션이 적용된 화면이 없기 때문에 우선 이 정도만 수행해도 충분하다고 생각한다. 화면을 구성하는 클라이언트에서는 현재
     * 페이지 인덱스와 이 메서드를 통해 최종적으로 전달된 마지막 페이지 인덱스가 같을 경우에만 마지막 페이지를 처리를 하면 그만이다. 후에
     * 페이지 번호 표시가 필요할 경우 이를 다루겠다.
     * 
     * {@code perPageCnt} 값이 null일 경우 {@code 1}을 반환하는 이유는, {@code perPageCnt}가 null일
     * 경우는 현재의 지상 와이어 프레임의 일반적인 상품 목록 화면과 같이 페이지네이션이 적용되지 않은 혹은 한 페이지에 모든 상품 목록을 다
     * 출력하는 스타일 화면에 대한 요청의 경우이다. 이 경우에 마지막 페이지는 1 페이지일 것이다. 페이지 뷰의 경우 클라이언트에 반환 될
     * {@link PagenationProductListView} 클래스 오브젝트의 프로퍼티 설정을 위해 아래 메서드가 사용된다.
     * 
     * 
     */
    public int getEndPage() {
        if (perPageCnt == null) {
            return 1;
        }
        return (int) Math.ceil(totalProductCnt / perPageCnt);
    }

    /**
     * 
     * {@link PageViewCriteriaBuilder}에 대한 빌더 클래스이다. 이 클래스의 생성자 및 수정자 메서드에서 전달된 인자에
     * 대한 유효성 검사를 수행함을 알 수 있는데, 사실 이 클래스에 전달되는 값은 모두 컨틀롤러 단에서의 bean validation에 통과한
     * 값들이다. 그러나 혹시 모를 코드의 변경으로 인한 잘못된 값의 전달에 따른 오동작을 방지하고자 검사를 하였다. 확실하게 안전함을 보장할 수
     * 있기 때문이다. 그러나, 테스트에 거쳐 코드 문제가 없음을 알며 동시에 bean validation을 통과하였음까지 아는 상황에서 또 한번
     * 검사하는 것은 불필요한 것은 아닌가란 생각도 들어 정답을 모르겠다.
     * 
     * @author leeseunghyun
     *
     */
    public static class PageViewCriteriaBuilder extends ProductListViewCriteriaBuilder {

        private static final Logger logger = LoggerFactory.getLogger(PageViewCriteriaBuilder.class);

        private static final Integer DEFAULT_PAGE_INDEX = 1;
        // private static final int DEFAULT_DISPLAY_PAGE_COUNT = 10;

        private Integer totalProductCnt;

        private Integer pageIndex = DEFAULT_PAGE_INDEX;
        private Integer perPageCnt;
        // private int displayPageCnt = DEFAULT_DISPLAY_PAGE_COUNT;

        public PageViewCriteriaBuilder(Integer marketId, String mallLocation, String category,
                Integer totalProductCnt) {
            super(marketId, mallLocation, category);
            this.totalProductCnt = totalProductCnt;

            if (this.totalProductCnt < 0) {
                logger.error(
                        "Invalid argument totalProductCnt detected : {}. totalProductCnt must not be negative value.",
                        totalProductCnt);
                logger.error("Checking illegal code required.");

                throw new IllegalStateException("Invalid argument totalProductCnt detected.");
            }
        }

        public PageViewCriteriaBuilder pageIndex(Integer pageIndex) {

            this.pageIndex = pageIndex;
            return this;
        }

        public PageViewCriteriaBuilder perPageCnt(Integer perPageCnt) {

            this.perPageCnt = perPageCnt;
            return this;
        }

        @Override
        public PageViewCriteriaBuilder searchKeyword(String searchKeyword) {
            this.searchKeyword = searchKeyword;
            return this;
        }

        /*
         * public PageViewCriteriaBuilder displayPageCnt(Integer displayPageCnt) {
         * 
         * this.displayPageCnt = displayPageCnt; return this; }
         */

        /**
         * {@link PageViewCriteria} 오브젝트를 생성하여 반환한다.
         */
        @Override
        public PageViewCriteria build() {
            PageViewCriteria criteria = new PageViewCriteria(this);

            if (criteria.pageIndex <= 0) {
                logger.error("Invalid argument pageIndex detected : {}. pageIndex must not be zero or negative value."
                           , pageIndex);
                logger.error(
                          "Because arguent pageIndex passed to bean validation, It must be caused by illegal code. "
                        + "Checking illegal code required.");
                throw new IllegalStateException("Invalid argument pageIndex detected.");
            } else if (criteria.perPageCnt != null && criteria.perPageCnt <= 0) {
                logger.error(
                            "Invalid argument perPageCnt detected : {}. perPageCnt must not be zero or negative value."
                            , perPageCnt);
                logger.error(
                        "Because arguent perPageCnt passed to bean validation, It must be caused by illegal code. "
                      + "Checking illegal code required.");
                throw new IllegalStateException("Invalid argument perPageCnt detected.");
            } /*
               * else if(criteria.displayPageCnt <= 0) { logger.
               * error("Invalid argument displayPageCnt detected : {}. perPageCnt must not be zero or negative value."
               * , displayPageCnt); logger.
               * error("Because arguent displayPageCnt passed to bean validation, It must be caused by illegal code. Checking illegal code required."
               * ); throw new
               * IllegalStateException("Invalid argument displayPageCnt detected."); }
               */

            return criteria;
        }

    }

    @Override
    public String toString() {
        return super.toString() + "[pageIndex=" + pageIndex + ", perPageCnt=" + perPageCnt 
                                + ", totalProductCnt=" + totalProductCnt // + ", displayPageCnt=" + displayPageCnt 
                                + "]";
    }
}
