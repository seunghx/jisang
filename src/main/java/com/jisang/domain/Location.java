package com.jisang.domain;

import java.util.List;


/**
 * 
 * 후의 지도 기능을 확장할 경우, List<Market> 타입의 프로퍼티 보다는 List<Address> 타입의 프로퍼티를 갖는 것이 더 유연하다고 생각하였으며
 * 지도 기능의 입장에서 볼 때 도메인은 이 클래스 {@link Location}과 {@link Address}이지 {@link Market}은 아니라고 생각하였다.
 * 또한 {@link Market} 타입의 리스트를 가질 경우 지상 어플리케이션을 이용하지 않는 지하상가 상점에 대한 정보를 지도에 띄울 필요가 있다고 할 때 
 * 이러한 기능이 불가능하다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class Location {
	
	private String id;
	
	private double topLeftLAT;
	private double topLeftLNG;
	
	private double bottomRightLAT;
	private double bottomRightLNG;
	
	private List<Address> addressList;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public double getTopLeftLAT() {
		return topLeftLAT;
	}
	
	public void setTopLeftLAT(double topLeftLAT) {
		this.topLeftLAT = topLeftLAT;
	}
	
	public double getTopLeftLNG() {
		return topLeftLNG;
	}
	
	public void setTopLeftLNG(double topLeftLNG) {
		this.topLeftLNG = topLeftLNG;
	}
	
	public double getBottomRightLAT() {
		return bottomRightLAT;
	}
	
	public void setBottomRightLAT(double bottomRightLAT) {
		this.bottomRightLAT = bottomRightLAT;
	}
	
	public double getBottomRightLNG() {
		return bottomRightLNG;
	}
	
	public void setBottomRightLNG(double bottomRightLNG) {
		this.bottomRightLNG = bottomRightLNG;
	}
	
	public List<Address> getAddressList() {
		return addressList;
	}
	
	public void setAddressList(List<Address> addressList) {
		this.addressList = addressList;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[id=" + id + ", topLeftLAT=" + topLeftLAT + ", topLeftLNG=" + topLeftLNG 
									+ ", bottomRightLAT=" + bottomRightLAT + ", bottomRightLNG=" + bottomRightLNG 
									+ ", addressList=" + addressList + "]";
	}
}
