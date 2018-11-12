package com.jisang.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jisang.dto.ErrorDTO;
import com.jisang.support.NoSuchMarketException;
import com.jisang.support.NoSuchProductException;
import com.jisang.support.UnsupportedImageFormatException;
import com.jisang.support.validation.FieldErrorBasedValidationException;

/**
 * 
 * 어플리케이션에서 발생하는 예외에 대한 최종 처리를 수행한다. 이 클래스가 계승하는
 * {@link ResponseEntityExceptionHandler}의 메서드를 오버라이딩 해서 예외를 처리하며 기타
 * {@link RuntimeException} 하위의 다른 예외들에 대해서는 이 클래스의
 * {@link #handleSystemException}에서 처리한다.
 * 
 * 이 클래스의 메서드에서 예외 처리의 절차로 로깅을 함을 알 수 있다.
 * {@link Logger#error(String, Throwable)}와 같은 방법을 사용하고 있는데, 이 메서드의 경우 예외의 스택
 * 트레이스가 콘솔에 출력되게 된다. 서버 내부에서 발생한 예외가 아닌 {@link BindException}과 같은 예외의 경우에는 간단한
 * 원인만 로그로 남기고 적절한 응답만 클라이언트에게 보낸다면 충분할 것 같다고 생각되나 정답을 몰라 우선은 위의 방법으로 로깅을 진행하였다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private MessageSource msgSource;

    /**
     * 
     * 컨트롤러 메서드에 전달되는 인자에 문제가 있을 때 던져지는 예외에 대한 예외 핸들러이다. 예를 들면
     * {@code @PathVariable}인자에 다른 타입의 값이 들어왔다거나 할 때 이 예외가 발생한다.
     * 
     * @param status
     *            - 400 - Bad Request 를 의미.
     * 
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("An exception occurred associated with controller method parameter.", ex);

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.MethodArgumentNotValidException", null, request.getLocale()));

        ex.getBindingResult().getFieldErrors().stream()
                .forEach(e -> errDTO.addDetail(e.getField(), msgSource.getMessage(e, request.getLocale())));

        ex.getBindingResult().getGlobalErrors().stream()
                .forEach(e -> errDTO.addDetail(e.getObjectName(), msgSource.getMessage(e, request.getLocale())));

        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * 컨트롤러에 전달되는 오브젝트 인자에 대한 바인딩이 실패할 경우 이 메서드에서 해당 예외를 처리한다.
     * 
     * @param status
     *            - 400 - Bad Request 를 의미.
     */
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        logger.error("An Exception occurred while trying to bind object", ex);

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.BindException", null, request.getLocale()));

        ex.getBindingResult().getFieldErrors().stream()
                .forEach(e -> errDTO.addDetail(e.getField(), msgSource.getMessage(e, request.getLocale())));

        ex.getBindingResult().getGlobalErrors().stream()
                .forEach(e -> errDTO.addDetail(e.getObjectName(), msgSource.getMessage(e, request.getLocale())));

        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * {@link HttpServletRequest}에 원하는 파라미터가 들어있지 않을 경우 발생하는 예외를 처리한다.
     * 
     * @param status
     *            - 400 - Bad Request 를 의미.
     *
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("An exception occurred because request parameter doesn't exist.", ex);

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.MissingServletRequestParameterException",
                        new String[] { ex.getParameterName() }, request.getLocale()));

        return handleExceptionInternal(ex, errDTO, headers, status, request);

    }

    /**
     * 
     * 요구한 multipart file이 전달되지 않은 경우 발생하는 예외를 처리한다.
     * 
     * @param status
     *            - 400 - Bad Request 를 의미.
     * 
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("An exception occurred because request multipart data doesn't exist.", ex);

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.MissingServletRequestPartException",
                        new String[] { ex.getRequestPartName() }, request.getLocale()));

        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * 컨트롤러의 {@link @RequestMapping} 메서드의 인자로 전달 될 경로 변수가 URL에서 추출 된 URI 변수에 존재하지 않을
     * 때 발생하는 예외를 처리한다. 이 예외는 일반적으로 URI 템플릿이 method 인자에 명시된 경로 변수 이름과 매치되지 않음을 의미한다.
     * 
     * @param status
     *            - 500 - Internal Server Error를 의미. URI 템플릿에서 지정한 경로 변수 명이나 메서드
     *            인자명을 바꾸거나 해야 한다.
     * 
     */
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        logger.error("An exception occurred accoicated with path variable setting.", ex);

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.MissingPathVariableException", null, request.getLocale()));

        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * 페이지를 찾지 못할 때 발생하는 예외에 대한 처리를 수행하는 메서드.
     * 
     * @param status
     *            - 404 - Not Found
     * 
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.NoHandlerFoundException", new String[] { ex.getRequestURL() },
                        request.getLocale()));
        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * 지원되지 않는 HTTP method가 컨트롤러에 전달되었을 때 발생하는 예외를 처리하는 메서드.
     * 
     * @param status
     *            - 405 - Method Not Allowed
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("An exception occurred associated with unsupported type request.", ex);

        StringBuilder builder = new StringBuilder();

        ex.getSupportedHttpMethods().forEach(method -> builder.append(method.name() + ", "));
        int lastCommaIdx = builder.lastIndexOf(",");
        builder.replace(lastCommaIdx, lastCommaIdx + 1, "");

        ErrorDTO errDTO = new ErrorDTO(status.value(),
                msgSource.getMessage("response.exception.HttpRequestMethodNotSupportedException",
                        new String[] { ex.getMethod(), builder.toString() }, request.getLocale()));
        return handleExceptionInternal(ex, errDTO, headers, status, request);
    }

    /**
     * 
     * 핸들러(컨트롤러) 메서드 인자에 대한 binding 과정이 아닌 service 계층과 같은 다른 곳에서의 bean validation 중
     * 예외가 발생할 경우 이는 {@link FieldErrorBasedValidationException}로 변환 된다. 이 예외를 아래의
     * 메서드가 처리한다.
     * 
     */
    @ExceptionHandler(FieldErrorBasedValidationException.class)
    public ResponseEntity<ErrorDTO> handleFieldErrorBasedValidationException(FieldErrorBasedValidationException ex,
            WebRequest request) {
        logger.error("An exception occurred associated with bean validation.", ex);

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), msgSource
                .getMessage("response.exception.FieldErrorBasedValidationException", null, request.getLocale()));

        ex.getFieldExceptions().stream()
                .forEach(e -> errDTO.addDetail(e.getField(), msgSource.getMessage(e, request.getLocale())));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 전달된 {@link MultipartFile} 오브젝트가 이미지 파일에 대한 멀티 파트 파일인지를 검사한 후 이에 실패할 때 던져지는
     * 예외이다. 해당 예외는 {@link BindingException}와 같이 핸들러 메서드 바인딩 과정에 발생하지 않고 서비스 등의 그 뒷
     * 단의 계층에서 validation을 수행할 때 발생하는 예외이다. 현재는 마켓 정보 수정에만 이 예외가 던져지나 후에 다른 곳에서도 던져질
     * 수 있을 것 같아 이 클래스 {@link GlobalExceptionHandler}에 정의하였다.
     */
    @ExceptionHandler(UnsupportedImageFormatException.class)
    public ResponseEntity<ErrorDTO> handleUnsupportedImageFormatException(UnsupportedImageFormatException ex,
            WebRequest request) {
        logger.error("An Exception to be processed", ex);

        StringBuilder builder = new StringBuilder();
        ex.supportedExtensions().stream().forEach(ext -> builder.append(", ").append(ext));

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                msgSource.getMessage("response.exception.UnsupportedImageFormatException",
                        new String[] { builder.substring(2) }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 
     * 마켓 정보가 이용되는 여러 로직에서 foreign key 제약 조건이 설정되어 있는 테이블로의 데이터 삽입 과정 중
     * {@link DataIntegrityViolationException}이 발생할 수 있다. 즉, 존재하지 않는 마켓 id를 가리키는 칼럼이
     * 있는 로우를 새로 삽입하려고 할 때 서비스 오브젝트에서는 위 예외를 {@link NoSuchMarketException} 으로 포장하여
     * 던진다. 아래 메서드는 이로부터 전달받은 예외 {@link NoSuchMarketException}을 처리한다.
     * 
     */
    @ExceptionHandler(NoSuchMarketException.class)
    public ResponseEntity<ErrorDTO> onNoSuchMarket(NoSuchMarketException ex, WebRequest request) {
        logger.error("An exception occurred associated with using non-existing market id.", ex);

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                msgSource.getMessage("response.exception.NoSuchMarketException",
                        new String[] { String.valueOf(ex.getMarketId()) }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 
     * 상품 정보가 이용되는 여러 로직에서 foreign key 제약 조건이 설정되어 있는 테이블로의 데이터 삽입 과정 중
     * {@link DataIntegrityViolationException}이 발생할 수 있다. 즉, 존재하지 않는 마켓 id를 가리키는 칼럼이
     * 있는 로우를 새로 삽입하려고 할 때 서비스 오브젝트에서는 위 예외를 {@link NoSuchMarketException} 으로 포장하여
     * 던진다. 아래 메서드는 이로부터 전달받은 예외 {@link NoSuchProductException}을 처리한다.
     * 
     */
    @ExceptionHandler(NoSuchProductException.class)
    public ResponseEntity<ErrorDTO> onNoSuchProduct(NoSuchProductException ex, WebRequest request) {
        logger.error("An exception occurred associated with using non-existing product id.", ex);

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                msgSource.getMessage("response.exception.NoSuchMarketException",
                        new String[] { String.valueOf(ex.getProductId()) }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 
     * 이 클래스가 계승하는 {@link ResponseEntityExceptionHandler}에 정의된 메서드에서 처리할 수 있는 예외가 아닌
     * 그 외의 예외에 대한 처리를 수행하는 메서드이다.
     * 
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleSystemException(Exception ex, WebRequest request) {
        logger.error("An system exception occurred.", ex);

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                msgSource.getMessage("response.exception.SystemException", null, request.getLocale()));

        return handleExceptionInternal(ex, errDTO, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
