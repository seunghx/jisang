package com.jisang.persistence;

import java.util.List;

import com.jisang.domain.Comment;

/**
 * 
 * 지상 어플리케이션의 Comment(댓글) 도메인 관련 DAO 인터페이스.
 * 
 * @author leeseunghyun
 *
 */
public interface CommentDAO extends MybatisMapper {
	
	public void create(Comment comment);
	public void updateParentId(int parentId);
	
	
	public Comment read(int commentId);
	public List<Comment> readList(int productId);
}
