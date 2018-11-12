package com.jisang.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * 지하 상가 마켓에서 판매되는 상품에 대한 도메인 클래스
 * 
 * 
 * @author leeseunghyun
 *
 */
public class Product {

    private int id;
    private int marketId;

    private String category;
    private String name;
    private String price;
    private String detail;
    private LocalDateTime uploadTime;

    private String representingImageUrl;

    /**
     * 
     * 지상 어플리케이션의 데이터 베이스에는 tbl_hashtags, tbl_images라는 테이블이 존재한다. 각각 상품의 해시태그 목록,
     * 상품의 이미지 목록을 담고 있다. 하나의 상품에 대하여 여러 개의 이미지와 해시태그가 존재하기에 따로 분리해 놓은 테이블에 불과하고
     * 아직까지 단순히 상품을 구성하는 정보 이상의 의미가 없다고 생각하여 이들을 도메인 클래스로 정의하지 않았다. 그러므로 이 클래스의 프로퍼티
     * {@code imageUrls, hashTags}를 예를 들어 {@code List<ProductImage>,
     * List<HashTag>}가 아닌 {@code List<String>} 타입으로 선언 하였다. 후에 새로운 요구사항이 생길 경우 도메인
     * 클래스로 변경해봐야 겠다.
     * 
     */
    private List<String> hashTags;
    private List<String> imageUrls;

    private boolean recommended;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(List<String> hashTags) {
        this.hashTags = hashTags;
    }

    public String getRepresentingImageUrl() {
        return representingImageUrl;
    }

    public void setRepresentingImageUrl(String repreentingImageUrl) {
        this.representingImageUrl = repreentingImageUrl;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id= " + id + ", category=" + category + ", name=" + name + ", price=" + price
                + ", detail=" + detail + ", marketId=" + marketId + ", uploadTime=" + uploadTime
                + ", representingImage=" + representingImageUrl + ", image=" + imageUrls + ", hashTags=" + hashTags
                + ", recommended=" + recommended + "]";
    }

}
