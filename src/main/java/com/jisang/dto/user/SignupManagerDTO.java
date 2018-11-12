package com.jisang.dto.user;

import javax.validation.constraints.NotBlank;

import com.jisang.support.validation.ExistingMallLocation;

import io.swagger.annotations.ApiModelProperty;




/**
 * 
 * 상점 관리자 회원 등록에 사용될 정보를 담는 DTO 클래스.
 * 
 * @author leeseunghyun
 *
 */
public class SignupManagerDTO extends SignupDTO {


	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================
	@ApiModelProperty(notes = "등록할 마켓 이름.", name = "marketName", required = true, example = "이승현 마켓")
	@NotBlank(message = "상점 이름을 입력하세요.")
	private String marketName;
	
	@ApiModelProperty(notes = "마켓 위치 코드", name = "marketLocation", required = true, example = "강남 : '11'")
	@NotBlank(message = "지하상가의 위치 코드를 입력하세요.")
	@ExistingMallLocation(message = "올바른 지하상가 정보를 입력하세요.")
	private String marketLocation;
	
	@ApiModelProperty(notes = "지하상가 내 마켓 위치 정보.", name = "marketAddress", required = true, example = "B-12")
	@NotBlank(message = "마켓 주소를 입력하세요.")
	private String marketAddress;
	
	
	// Constructors
	//==========================================================================================================================
	
	
	// Methods
	//==========================================================================================================================

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getMarketLocation() {
		return marketLocation;
	}

	public void setMarketLocation(String marketLocation) {
		this.marketLocation = marketLocation;
	}

	public String getMarketAddress() {
		return marketAddress;
	}

	public void setMarketAddress(String marketAddress) {
		this.marketAddress = marketAddress;
	}
	
	@Override
	public String toString() {
		String ret = super.toString().substring(0, super.toString().length()-1);
		return ret + ", marketName=" + marketName + ", marketLocation=" + marketLocation + ", marketAddress=" + marketAddress + "]";
	}
}
