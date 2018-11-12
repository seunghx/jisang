package com.jisang.config.code;

/**
 * 
 * 어플리케이션에서 사용되는 코드가 갖추어야할 메서드를 정의한 인터페이스이다. enum 으로 정의되어있는 어플리케이션의 코드는 
 * enum의 추상 메서드를 사용하여 메서드를 구현하게 하는 방법도 있으나 {@link CodeBook}의 enum 클래스들과 같이 토픽에 따라 분리 되어 있는
 * 여러 종류의 enum에 대하여 공통적으로 메서드가 구현되게 하려면 인터페이스를 이용하는 것이 낫겠다는 생각에 이 인터페이스를 정의하게 되었다.
 * 
 * @author leeseunghyun
 *
 */
public interface AppCode {
	/**
	 * for JSON serialization
	 */
	public String getName();
	
	/**
	 * for JSON serialization
	 */
	public String getCode();

	public boolean equalsByCode(String code);
}
