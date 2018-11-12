package com.jisang.domain;

import java.util.List;

/**
 * 
 * 지하상가 마켓을 나타내는 도메인 클래스이다.
 * 
 * 지상 어플리케이션의 와이어프레임을 보면 알 수 있듯 마켓을 구성하는 정보가 아직은 많지 않으나 쇼핑몰 어플리케이션에서 상점(마켓)의 정보는
 * 중요한 정보인 만큼 실제 서비스가 진행된다면 더 많은 정보를 담아야 할 것이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class Market {

    // Static Fields
    // ==========================================================================================================================

    // Instance Fields
    // ==========================================================================================================================

    private int id;
    private String name;

    /**
     * 
     * {@link Market}에 {@code location} 프로퍼티를 둔 이유
     * 
     * : 지역별 상품 검색 쿼리에서 join을 한 차례 줄이고자 tbl_markets에 tbl_locations을 가리키는(foreign
     * key)칼럼을 추가하였다. Mybatis와 같은 데이터 엑세스 레이어에서는 입/출력으로 도메인 오브젝트만 사용하기로 결정하였기 때문에 앞서
     * 말한 칼럼에 정보를 추가 또는 변경하려면 이 클래스 {@link Market}에 지역 정보 id를 담는 프로퍼티가 필요하다. 또한 단지
     * 데이터베이스 칼럼 상에 tbl_locations의 id만을 담을 목적이기 때문에 도메인 클래스 {@link Location} 타입의
     * 프로퍼티가 아닌 지역 id를 나타내는 문자열 타입을 프로퍼티를 정의하였다. 도메인 클래스 {@link Location}은 지도 기능에서만
     * 의미를 갖는다.
     * 
     * 
     * (사용자 쇼핑 화면에 보여지는 마켓 정보 화면에 지하상가의 위도 경도 값이 출력 될 필요가 없을 것이기 때문.)
     * 
     */
    private String location;
    private String imageUrl;
    private List<Product> products;

    // Constructors
    // ==========================================================================================================================

    // Methods
    // ==========================================================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[id=" + id + ", location=" + location + ", name=" + name 
                                         + ", imageUrl=" + imageUrl + ", products =" + products + "]";
    }
}
