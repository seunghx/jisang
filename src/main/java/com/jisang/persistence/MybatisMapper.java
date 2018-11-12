package com.jisang.persistence;

import org.mybatis.spring.annotation.MapperScan;

/**
 * 
 * Mybatis의 {@link MapperScan} 애노테이션이 인자로 전달된 패키지 내의 모든 인터페이스를 Mapper로 취급하기 때문에,
 * 이런 동작을 방지하기 위해 정의한 마커 인터페이스이다.
 * 
 * @author leeseunghyun
 *
 */
public interface MybatisMapper {

}
