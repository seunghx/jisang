package com.jisang.persistence;

import com.jisang.domain.User;

/**
 * 
 * 지상 어플리케이션의 User 도메인 관련 DAO 인터페이스.
 * 
 * @author leeseunghyun
 *
 */
public interface UserDAO extends MybatisMapper {
	public User read(int uid);
	public void create(User user);
	public void update(User user);
}
