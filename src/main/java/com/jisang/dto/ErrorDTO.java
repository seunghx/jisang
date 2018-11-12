package com.jisang.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;



/**/
/**
 * 
 * 웹 앱에서 발생한 오류(예외) 정보를 담은 DTO 클래스. 
 * 
 * @author leeseunghyun
 *
 */
public class ErrorDTO {

	
	// Static Fields
	//==========================================================================================================================

	
	// Instance Fields
	//==========================================================================================================================
	
	private int status;
	private String message;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<Detail> details = new ArrayList<>();


	// Constructors
	//==========================================================================================================================
	
	public ErrorDTO() {}
	
	public ErrorDTO(int status, String message) {
		this.status = status;
		this.message = message;
	}
	

	// Methods
	//==========================================================================================================================

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addDetail(String target, String message) {
		
		details.add(new Detail(target, message));
	}
	
	public List<Detail> getDetails(){
		return details;
	}
	
	// Methods
	//==========================================================================================================================
	
	/**
	 * 
	 * 바인딩 과정에 예외가 발생할 경우 예외가 발생한 위치와 해당 프로퍼티에 지정된 검증 메세지 정보를 담는다.
	 * 
	 * @author leeseunghyun
	 *
	 */
	private static class Detail {
		
		private final String target;
		private final String message;
		
		Detail(String target, String message) {
			this.target = target;
			this.message = message;
		}
		
		@SuppressWarnings("unused")
		public String getTarget() {
			return target;
		}
		
		@SuppressWarnings("unused")
		public String getMessage() {
			return message;
		}
		
		@Override
		public String toString() {
			return this.getClass().getName() + "[targetProperty=" + target + ", message=" + message + "]";
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[status=" + status + ", message=" + message + ", details=" + details + "]";
	}
	
}
