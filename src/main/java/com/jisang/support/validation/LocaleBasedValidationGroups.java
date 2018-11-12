package com.jisang.support.validation;

/**
 * 
 * 핸드폰 번호의 국가 코드 등에 대한 bean validation을 수행에 사용되는 validation grouping 용 인터페이스이다.
 * 
 * 10.05) 각 지역 별로 서버가 존재한다면 (한국 서버, 일본 서버 등) 현재 구현과 같이 클라이언트로부터 locale을 전달 받아
 * locale에 따라 핸드폰 번호의 validation을 수행하는 방법보다는 각 서버에서 그 서버의 default locale에 해당하는
 * 핸드폰 번호 validation을 수행하는 것이 더 알맞을 수 있겠다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface LocaleBasedValidationGroups {

}
