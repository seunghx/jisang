package com.jisang.dto.product;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 일반 유저의 쇼핑 화면 중 상품 상세 화면에 필요한 정보를 담는 DTO 클래스이다.
 * 
 * @author leeseunghyun
 *
 */
public class ProductShoppingDetailedViewDTO {
	
	@ApiModelProperty(notes = "해당 상품을 판매하는 마켓 id.", name = "marketId")
	private int marketId;
	@ApiModelProperty(notes = "해당 상품을 판매하는 마켓 이름.", name = "marketName")
	private String marketName;
	
	@ApiModelProperty(notes = "해당 상품의 id.", name = "productId")
	private int productId;
	@ApiModelProperty(notes = "해당 상품의 이름.", name = "productName")
	private String productName;
	@ApiModelProperty(notes = "상품 상품의 가격.", name = "price")
	private String price;
	@ApiModelProperty(notes = "해당 상품에 대한 설명.", name = "detail")
	private String detail;
	
	@ApiModelProperty(notes = "상품 대표 이미지 url.", name = "representingImageUrl")
	private String representingImageUrl;
	@ApiModelProperty(notes = "상품 이미지 url.", name = "imageUrls")
	private List<String> imageUrls;
	
	public int getMarketId() {
		return marketId;
	}
	
	public void setMarketId(int marketId) {
		this.marketId = marketId;
	}
	
	public String getMarketName() {
		return marketName;
	}
	
	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}
	
	public int getProductId() {
		return productId;
	}
	
	public void setProductId(int productId) {
		this.productId = productId;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
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
	
	public String getRepresentingImageUrl() {
		return representingImageUrl;
	}
	
	public void setRepresentingImageUrl(String representingImageUrl) {
		this.representingImageUrl = representingImageUrl;
	}
	
	public List<String> getImageUrls() {
		return imageUrls;
	}
	
	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[marketId=" + marketId + ", marketName=" + marketName + ", productId=" + productId 
									+ ", productName=" + productName + ", price=" + price + ", detail=" + detail
									+ ", representingImageUrl=" + representingImageUrl + ", imageUrls=" + imageUrls + "]";
	}
	
	
}
