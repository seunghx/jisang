package com.jisang.security.support;

/**
 * 
 * redis key로 사용 될 문자열을 정의한 클래스.
 * 
 * spring data redis 샘플 프로젝트의 {@code KeyUtils}클래스를 보고 정의하게 된 클래스이다. 샘플 프로젝트의 {@code KeyUtils}는 redis의 String 타입 
 * 데이터가 사용될 것으로 고려하여 key 네이밍을 한 것 같다. 그러나 나는 redis Hash를 사용할 것이기 때문에 (최소한 JTI 검사를 통한 유저 인증에서는) 샘플 
 * 프로젝트와는 약간 다르게 정의하였다.
 * 
 * 이 클래스를 사용하면 redis에서 사용되는 전체 key(key name)에 대한 관리를 한 곳에서 수행할 수 있어 (그럴 일 없겠지만)실수로 key나 Hash key(field)의 중복 등이 
 * 발생할 여지가 적다. 또한 redis의 기존 key name을 변경할 일이 있다면 이 클래스 이용 방식이 훨씬 변경이 편할 것이다. 그러나 key 또는 Hash key(field)가 
 * 변경(및 추가)될 때마다 이 클래스도 변경되어야 한다는 점에서 단점도 있다. key와 hash key가 변경된다면 어플리케이션 코드도 변경되어야하는 것이야 당연하나 이 클래스를
 * 이용하지 않았다면 변경 요소는 {@link RedisTemplate} 등을 직접 이용하는 곳 뿐일 것이다. 그러나 이 정도의 변경 요소 추가는 큰 불편 거리는 아닌 것 같다. 
 * 
 * 
 * @author leeseunghyun
 *
 */
public class RedisKeyUtils {
	
	/** user에 대한 key prefix */
	private static final String USER = "user:";
	
	public static String userKey(int uid) {
		return USER + uid;
	}
	
	/**
	 * 
	 * redis Hash 타입의 Hash key name(field name)으로 사용될 문자열을 정의한 클래스.
	 * 
	 * @author leeseunghyun
	 *
	 */
	public static class HashKeyUtils {
		/** jti에 대한 field name */
		private static final String JTI_FIELD = "jti";
		
		public static String jtiField() {
			return JTI_FIELD;
		}
	}	
	
}
