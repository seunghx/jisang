package com.jisang.web.comment;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;


import com.jisang.dto.ErrorDTO;
import com.jisang.dto.comment.CommentResponseDTO;
import com.jisang.dto.comment.CommentRegisterDTO;
import com.jisang.service.comment.CommentService;
import com.jisang.support.NestedCommentException;
import com.jisang.support.NoSuchParentCommentIdException;
import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(description = "Comment related API")
public class CommentController {
	
	private final Logger logger = LoggerFactory.getLogger(CommentController.class);
	
	@Autowired
	private CommentService commentService;
	@Autowired
	private MessageSource msgSource;
	
	
	/**
	 * 
	 * 현재 지상 어플리케이션에서 허용하는 댓글 깊이(댓글에 대한 댓글)를 초과한 깊이의 댓글이 발견될 경우 {@link NestedCommentException}이 던져진다.
	 * 아래 메서드는 이 예외를 처리한다.
	 *
	 */
	@ExceptionHandler(NestedCommentException.class)
	public ResponseEntity<ErrorDTO> onNestedComment(NestedCommentException ex, WebRequest request){
		logger.error("An exception occurred associated with invalid comment hierarchy on comment registering.", ex);	
		
		ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value()
									 , msgSource.getMessage("response.exception.NestedCommentException", null, request.getLocale()));
		
		return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NoSuchParentCommentIdException.class)
	public ResponseEntity<ErrorDTO> onNoSuchParentCommentId(NoSuchParentCommentIdException ex, WebRequest request){
		logger.error("An exception occurred associated with invalid parent comment id on comment registering.", ex);	
		
		ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value()
									 , msgSource.getMessage("response.exception.NoSuchParentCommentIdException", null
											 			  , request.getLocale()));
		
		return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
		
	}
	
	/**
	 * 
	 * 지상 어플리케이션 상에 존재하지 않는 상품(id)에 대한 댓글 등록 요청이 발견되었을 때 {@link DataIntegrityViolationException}이 발생할 수 있다.
	 * 아래 메서드는 이 예외를 처리한다.
	 *
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorDTO> onDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request){
		logger.error("An exception occurred associated with invalid product id on comment registering", ex);	
		
		ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value()
									 , msgSource.getMessage("response.exception.DataIntegrityViolationException.comment", null, request.getLocale()));
		
		return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * 
	 * 댓글 등록 요청에 대한 핸들러 메서드이다.
	 * 
	 * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해 {@link Authentication} 타입 오브젝트에 대한 
	 * 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 
	 * 파라미터 {@code userId}가 전달 된다고 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 
	 * 않을 것이다.
	 * 
	 * 
	 * @param userId - {@link UserID}, {@link UserIDArgumentResolver}
	 * 
	 */
	@PostMapping("/auth/comment")
	@ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
	@ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized"),
					@ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @ApiOperation(value = "댓글 등록.")
	public ResponseEntity<Void> postComment(CommentRegisterDTO commentDTO, UserID userId){
		
		commentDTO.setUserId(userId.getUserId());
		commentService.registerComment(commentDTO);
		
		logger.debug("Registering comment succeeded.");
		
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	/**
	 * 
	 * 특정 상품에 해당하는 댓글 목록 조회 요청을 담당하는 핸들러 메서드이다.
	 * 
	 * url이 {@code /product/{productId}/comment}이긴 하나 댓글은 댓글 내용 외에도 작성자 id, 상품 id 등을 포함하므로 댓글을 하나의 도메인으로 여기게 되었다. 
	 * 그러므로 해당 url을 아래 메서드를 통해 이 클래스 {@link CommentController}에서 처리한다. 
	 * 
	 */
    @ApiOperation(value = "특정 상품의 댓글 목록 조회.", response = CommentResponseDTO.class)
    @ApiImplicitParam(name = "productId", value = "조회 될 댓글의 상품 id.", dataType = "int", paramType = "path", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK")})
	@GetMapping("/product/{productId}/comment")
	public ResponseEntity<List<CommentResponseDTO>> getCommentList(@PathVariable("productId")int productId){
		
    	List<CommentResponseDTO> commentList = commentService.findCommentList(productId);
    	
		logger.debug("Finding comments with product id : {} succeeded.", productId);
    	
    	return new ResponseEntity<>(commentList, HttpStatus.OK);
	}
	
}
