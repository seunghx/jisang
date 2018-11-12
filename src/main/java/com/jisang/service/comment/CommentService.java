package com.jisang.service.comment;

import java.util.List;

import com.jisang.domain.Comment;
import com.jisang.dto.comment.CommentResponseDTO;
import com.jisang.dto.comment.CommentRegisterDTO;


/**
 * 
 * {@link Comment} 도메인 관련 비즈니스 로직을 정의한 서비스 인터페이스이다.
 *
 *
 * @author leeseunghyun
 *
 */
public interface CommentService {
	public void registerComment(CommentRegisterDTO commentDTO);
	public List<CommentResponseDTO> findCommentList(int productId);
}
