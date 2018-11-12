package com.jisang.support;



/**
 *  
 *  
 * 존재하지 않는 지하상가 주소 코드가 전달될 경우 아래 예외가 사용될 수 있다.
 * 
 * {@link IllegalArgumentException}과 같은 보다 일반적인 예외를 던질 수도 있으나 {@link IllegalArgumentException}의 경우 상황에 따라 
 * 500 - Internal Server Error 응답 처리를 해야하는 경우도 있을거라고 생각되어 400 - Bad Request 응답으로 처리하기에 보다 적합한 예외를 던지기 위해 
 * 정의하였다.
 * 
 * 
 * 사실 아래 예외는 현재 {@link MapService} 구현 클래스에서만 사용된다.
 * 다른 경우에는 핸들러 메서드 파라미터에 대한 bean validation으로 잘못된 주소 코드가 전달될 때 {@link BindException}이 던져지게 되는 방식으로 잘못된 주소 
 * 코드에 대한 처리를 하는데 반해, 경로 변수로 마켓 id 또는 주소 코드를 전달 받는 {@link MapController}의 핸들러 메서드들의 구현상 bean validation이 
 * 불가능하기 때문에 존재하지 않는 주소 코드에 대한 검사를 서비스 레이어 오브젝트 등에서 수행하여 검사에 실패할 경우 경우 아래 예외가 던져지게 하였다. 
 * 
 * bean validation 적용이 어렵다는 것은 결국 API 설계(핸들러 엔드포인트 - 예 : '/map/market/{marketId}', '/map/location/{locationId}')
 * 가 잘못된 것이기 때문이라고 생각된다. 지도 정보를 불러오는 서비스 레이어 로직 상 '/map/market/{marketId}'의 경우 마켓 id가 데이터베이스에 존재하지 않으면  
 * 마켓 위치를 나타내는 마커만 클라이언트 화면에 표시되지 않게 할 수 있기 때문에 문제가 될 것이 없으므로 bean validation을 할 필요가 없으므로 이 경우에는 잘못되었다고
 * 할 수는 없겠다. 
 * 
 * 그러나 '/map/location/{locationId}'의 경우 locationId가 올바르지 않을 경우 잘못된 로케이션(지하상가)에 대한 지도 이미지와 지도 구성 정보를 클라이언트에 전달할 
 * 수 없기 때문에 bean validation이 비즈니스 로직 이전에 선행적으로 수행되는 것이 바람직하다. 그러므로 이 경우 API 설계(엔드포인트)가 잘못되었다고 봐야할 것 같다.
 * 
 * {@link ProductListViewConfigData}의 경우와 같이 DTO 안에 검색 타입을 의미하는 String 타입 프로퍼티를 갖는 방식은 직접 아래 예외를 던지는 방법보다
 * 불편하다고 생각했다. DTO에 지도 요청 타입을 나타내는 String 프로퍼티를 두는 방법은 커스텀 validator를 구현하여 직접 요청 타입 프로퍼티의 연관 프로퍼티
 * 와의 연관관계 검사까지 수행해야 한다.
 * (예를 들어, 요청 타입이 지역 코드를 통한 지도 정보 요청일 경우 지역 코드를 의미하는 프로퍼티가 요청 변수로 전달 되었는지에 대한 연관 관계 검증이 필요하다.)
 * 
 * 아니면 간단하게 {@link locationId}라는 프로퍼티를 딱 하나 갖는 DTO를 생성하고 '/map/location?locationId='와 같은 엔드포인트를 정의하여 요청 변수로 받는 방법
 * 도 있겠다. 그러나 '/map/location/{locationId}' 와 같은 방법이 적어도 나의 생각에는 직관적으로 보인다. 실제 서비스 되는 서비스 등의 API 및 요청 변수 설정 방법을
 * 직접 보고 무엇이 더 자주 사용되는지 또는 어떤 방법이 더 좋은지 고민해봐야 겠다.
 * 
 * 
 * @author leeseunghyun
 * 
 *
 */
public class NoSuchLocationException extends RuntimeException {

	private static final long serialVersionUID = 1365081625214728894L;
	
	private String locationId;
	
	public NoSuchLocationException(String message, String locationId) {
		super(message);
		this.locationId = locationId;
	}
	
	public NoSuchLocationException(String message, Throwable cause, String locationId) {
		this(message, locationId);
		initCause(cause);
	}
	
	public String getLocationId() {
		return locationId;
	}
}
