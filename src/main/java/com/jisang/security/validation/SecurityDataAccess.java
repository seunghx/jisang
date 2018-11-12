package com.jisang.security.validation;



/**
 * 
 * security 단에서 DAO를 사용하는 서비스 오브젝트 등에서 데이터베이스로부터 받아온 유저 정보에 대한 validation을 수행할 때 이 인터페이스를 
 * bean validation 그룹핑 용으로 이 인터페이스를 사용할 수 있다. 
 * 
 * 
 * @author leeseunghyun
 *
 */
public interface SecurityDataAccess extends UserAuthentication {

}
