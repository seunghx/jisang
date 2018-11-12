package com.jisang.domain;



/**
 * 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class Address {
	
	private String id;
	private String location;
	private Market market;
	
	private double topLeftLAT;
	private double topLeftLNG;
	private double bottomRightLAT;
	private double bottomRightLNG;	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[id=" + id + ", location=" + location + ", market=" + market 
									+ ", topLeftLAT=" + topLeftLAT + ", topLeftLNG=" + topLeftLNG 
									+ ", bottomRightLAT=" + bottomRightLAT + ", bottomRightLNG=" + bottomRightLNG + "]";
	}
	
}
