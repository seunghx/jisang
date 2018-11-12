package com.jisang.web;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jisang.config.code.CodeBook;
import com.jisang.dto.ErrorDTO;
import com.jisang.security.dto.SecurityErrorDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * 
 * 도메인 관련 컨트롤러에서 담당하는 엔드포인트를 제외한 나머지 엔드포인트에 대한 핸들러 메서드를 정의한 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@RestController
@Api(value= "info", description = "Resource unrelated API")
public class ApplicationController {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	@Autowired
	private CodeBook codeBook;
	
	
	/**
	 * 지상 어플리케이션 클라이언트와 서버 어플리케이션에서 사용되는 코드의 리스트들을 내부 프로퍼티로 담고 있는 {@link CodeBook}의 오브젝트를 응답으로 전달한다.
	 */
    @ApiOperation(value = "코드북 GET.", response = CodeBook.class)
	@ApiResponse(code = 200, message = "OK") 
	@GetMapping("/codebook")
	public ResponseEntity<CodeBook> getCodeBook() {
		return new ResponseEntity<>(codeBook, HttpStatus.OK);	
	}
	
	/**
	 * {@link AuthenticationNumberEntryPointFilter}는 인증 번호 발급 전 인증 번호를 발급하려는 목적지 정보에 대한 검증 및 인증 번호 발급을 수행한다.
	 * 이 필터 클래스는 성공적인 인증 및 인증 번호 발급을 마치면 즉시 클라이언트에게 응답을 보낸다. (컨트롤러를 거치지 않는다.) 그러므로 아래의 메서드는
	 * 실제로 호출되지는 않으나 API 문서 작성을 위해 선언하게 되었다.
	 */
    @ApiOperation(value = "인증 번호 발급.")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
					@ApiResponse(code = 400, message = "Bad Request", response = SecurityErrorDTO.class) })
	@ApiImplicitParams({
    	@ApiImplicitParam(name = "email", value = "인증 번호를 발급받으려는 유저 이메일", dataType = "string"
    					, paramType = "query", required = true),
    	@ApiImplicitParam(name = "destination", value = "인증 번호를 발급받으려는 유저의 핸드폰 번호(현재).", dataType = "string"
    					, paramType = "query", required = true),
	})
    
    
	@GetMapping("/authentication-number")
	public ResponseEntity<Void> fakeGetAuthenticationNumber() {
		logger.error("This method {} should not be called.", getClass().getSimpleName() + "#" + "fackGetAuthenticationNumber().");
		return new ResponseEntity<>(HttpStatus.OK);
	}
    
	
    @ApiOperation(value = "인증 번호 인증 요청.")
    @ApiImplicitParam(name = "destination", value = "인증 번호를 발급받으려는 유저의 핸드폰 번호(현재).", dataType = "string"
			, paramType = "query", required = true)
	@PostMapping("/authentication-number")
	public ResponseEntity<Void> fakePostAuthenticationNumber(){
		logger.error("This method {} should not be called.", getClass().getSimpleName() + "#" + "fackPostAuthenticationNumber().");
		return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    @GetMapping("/error")
    public ResponseEntity<ErrorDTO> error(){
    	
    	ErrorDTO  errDTO = new ErrorDTO();
    	errDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    	errDTO.setMessage("Server Error Occured.");
    	
    	return new ResponseEntity<>(errDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
