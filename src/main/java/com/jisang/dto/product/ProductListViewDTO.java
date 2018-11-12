package com.jisang.dto.product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 상품 목록 화면 구성에 필요한 DTO 클래스들을 정적 내부 클래스로 정의. 이들 DTO 클래스들은 마켓 관리 화면, 쇼핑 화면에 따라 둘로
 * 나뉜다.
 * 
 * @author leeseunghyun
 *
 */
public class ProductListViewDTO {

    /**
     * 
     * 상품 목록 화면에서의 각각의 상품에 대한 화면 구성에 필요한 정보를 담고 있는 DTO 클래스이다. 즉, 화면 상의 상품 목록은 이 DTO
     * 클래스의 목록이라고 할 수 있다. 상품 상세 정보와는 다르다.
     * 
     * @author leeseunghyun
     *
     */
    @Document(indexName = "products", type = "product")
    public static class ProductListViewEntity {

        @ApiModelProperty(notes = "상품 id.", name = "id")
        private int id;
        @ApiModelProperty(notes = "상품 대표 이미지 url.", name = "representingImage")
        private String representingImage;
        @ApiModelProperty(notes = "상품 이름.", name = "name")
        private String name;
        @ApiModelProperty(notes = "상품 가격.", name = "price")
        private String price;

        // @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        // private String marketName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getRepresentingImage() {
            return representingImage;
        }

        public void setRepresentingImage(String representingImage) {
            this.representingImage = representingImage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        /*
         * public String getMarketName() { return marketName; }
         * 
         * public void setMarketName(String marketName) { this.marketName = marketName;
         * }
         */

        @Override
        public String toString() {
            return getClass().getName() + "[id=" + id + ", representingImage=" + representingImage + ", name=" + name
                    + ", price=" + price + "]";
        }
    }

    /**
     * 
     * {@link ProductListViewEntity}의 리스트를 내부 프로퍼티로 담고 있다. 상품 목록 화면에는 이 클래스가 사용될
     * 것이다.
     * 
     * @author leeseunghyun
     *
     */
    public static class ProductListView {

        @ApiModelProperty(notes = "상품 정보 리스트", name = "productEntityList")
        protected List<ProductListViewEntity> productEntityList = new ArrayList<>();

        public void addProductEntity(ProductListViewEntity simpleProductEntity) {
            productEntityList.add(simpleProductEntity);
        }

        public List<ProductListViewEntity> getProductEntityList() {
            return productEntityList;
        }

        public void setProductEntityList(List<ProductListViewEntity> productEntityList) {
            this.productEntityList = productEntityList;
        }

        @Override
        public String toString() {
            return getClass().getName() + "[products=" + productEntityList + "]";
        }
    }

    /**
     * 
     * 페이지 타입 화면의 경우 상품 목록 정보와 함께 페이지 정보가 같이 전달되어야 한다. 이 DTO 클래스는 이를 목적으로 정의되었다. 현재
     * 구현은 현재 페이지 번호와 마지막 페이지 번호만 프로퍼티로 담고 있는데 모바일 화면 특징상 이 정도면 충분할 것 같다.
     * 
     * @author leeseunghyun
     *
     */
    public static class PagenationProductListView extends ProductListView {
        @ApiModelProperty(notes = "현재 페이지의 번호.", name = "currentPageIdx")
        private int currentPageIdx;
        @ApiModelProperty(notes = "마지막 페이지의 번호.", name = "endPageIdx")
        private int endPageIdx;

        public int getCurrentPageIdx() {
            return currentPageIdx;
        }

        public void setCurrentPageIdx(int currentPageIdx) {
            this.currentPageIdx = currentPageIdx;
        }

        public int getEndPageIdx() {
            return endPageIdx;
        }

        public void setEndPageIdx(int endPageIdx) {
            this.endPageIdx = endPageIdx;
        }

        @Override
        public String toString() {
            return super.toString() + "[currentPageIdx=" + currentPageIdx + ", endPageIdx=" + endPageIdx + "]";
        }

    }

    /**
     * 
     * 지상 어플리케이션 와이어 프레임의 관리자 화면을 보면 상품 등록 날짜 별로 그룹핑된 상품 목록에 대한 화면이 존재한다. 아래 DTO
     * 클래스는 이 경우를 위해 정의되었다.
     * 
     * @author leeseunghyun
     *
     */
    public static class ManagementProductListView extends ProductListView {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @ApiModelProperty(notes = "상품 등록 날짜", name = "uploadDate")
        private LocalDate uploadDate;

        public int getCount() {
            return productEntityList.size();
        }

        public LocalDate getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(LocalDate uploadDate) {
            this.uploadDate = uploadDate;
        }

        @Override
        public String toString() {
            return super.toString() + "[uploadDate=" + uploadDate + "]";
        }
    }
}
