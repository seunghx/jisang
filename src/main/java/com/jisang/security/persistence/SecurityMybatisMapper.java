package com.jisang.security.persistence;

import org.mybatis.spring.annotation.MapperScan;

/**
 * 
 * mybatis-spring의 어노테이션 {@link MapperScan}에 사용 될 마커 인터페이스이다. 가급적 security 단과 웹
 * 어플리케이션 단을 분리하는 생각에 seucirity 단에 추가로 정의하게 되었다.
 * 
 * @author leeseunghyun
 *
 */
public interface SecurityMybatisMapper {

    // Methods 
    // ==========================================================================================================================

}
