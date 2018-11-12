package com.jisang.support.conversion;

import java.util.Locale;
import org.springframework.format.Formatter;


/**
 * 
 * 핸드폰 번호에 대한 포매팅을 수행하는 인터페이스로 이 클래스의 구현은 핸드폰 번호를 의미하는 프로퍼티에 대한 포매팅 처리를 수행하는 
 * {@link Formatter} 구현 클래스인 {@link PhoneNumberFormatter} 클래스에서 사용 된다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface PhoneNumberFormatProvider {
	
	/**
	 * 요청 파라미터로부터 전달 받은 핸드폰 번호는 01063079376과 같이 하이픈 없이 번호로만 구성된 형태이다. 국가 코드 또한 없기에 이 메서드에서 
	 * 이를 포매팅한다.
	 */
	public String parse(String rawPhoneNumber);
	
	/**
	 * 포매팅 처리된 핸드폰 번호를 반대로 01063079376과 같이 번호로만 구성된 형태의 문자열로 변환시킨다.
	 */
	public String print(String formattedPhoneNumber);
	
	/**
	 * 이 인터페이스의 구현이 인자로 전달된 {@link Locale}을 지원 가능한지를 나타낸다. 핸드폰 번호의 형식은 국가마다 다르기 때문에 아래와 같은 메서드가 필요하다.
	 * 보통 {@code supports()} 메서드의 인자는 {@link Class<T>} 타입이 자주 사용되는 것 같다. 그러나 {@link Locale}타입 인자는 오브젝트를 인자로 받고
	 * {@code #equals} 메서드를 통한 비교를 하여도 충분한 것 같다.
	 */
	public boolean supports(Locale locale);
}
