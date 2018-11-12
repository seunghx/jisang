package com.jisang.web.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.jisang.dto.ErrorDTO;
import com.jisang.dto.market.MarketManagementDTO;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyRequestDTO;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyResponseDTO;
import com.jisang.service.market.MarketService;
import com.jisang.support.AddressAleadyUsedException;
import com.jisang.support.NoSuchAddressException;
import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(description = "Market related API")
public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    @Autowired
    private MessageSource msgSource;

    @Autowired
    private MarketService marketService;

    /**
     * 
     * 지상 어플리케이션의 데이터베이스 상에 존재하지 않는, 즉 지하상가에 실제 존재하지 않는 구역 주소 정보가 전달되었을 때
     * {@link NoSuchAddressException}이 던져진다.
     * 
     */
    @ExceptionHandler(NoSuchAddressException.class)
    public ResponseEntity<ErrorDTO> onNoSuchAddress(NoSuchAddressException ex, WebRequest request) {
        logger.error("An exception occurred associated with non-existing address.", ex);

        String address = ex.getAddress();
        String location = ex.getLocation();

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), msgSource.getMessage(
                "response.exception.NoSuchAddressException", new String[] { address, location }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 마켓 등록(매니저 회원가입) 또는 마켓 수정에 대한 요청 파라미터로 이미 다른 마켓에 의해 사용중인 점포 id값이 전달될 수 있다. 이
     * 경우 {@link AddressAleadyUsedException}이 발생한다.
     */
    @ExceptionHandler(AddressAleadyUsedException.class)
    public ResponseEntity<ErrorDTO> onAddressAleadyUsed(AddressAleadyUsedException ex, WebRequest request) {
        logger.error("An exception occurred associated with aleady used address.", ex);

        String address = ex.getAddress();
        String location = ex.getLocation();

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                msgSource.getMessage("response.exception.AddressAleadyUsedException",
                        new String[] { address, location }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * 
     * 지상 어플리케이션의 와이어프레임 상의 마켓 관리 화면에서 마켓 정보를 조회할 때 클라이언트로 호출되는 핸들러 메서드이다.
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해
     * {@link Authentication} 타입 오브젝트에 대한 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로
     * 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터 {@code userId}가 전달 된다고
     * 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 않을
     * 것이다.
     * 
     * 
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @ApiOperation(value = "마켓 정보 수정을 위한 마켓 정보 조회.", response = MarketModifyResponseDTO.class)
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @GetMapping("/auth/market")
    public ResponseEntity<MarketModifyResponseDTO> getMarketWithAuth(UserID userId) {

        int managerId = userId.getUserId();

        MarketModifyResponseDTO marketDTO = marketService.findMarketForManagement(managerId);
        logger.error(marketDTO.toString());
        logger.debug("Get market info succeeded. Processing http response building...");

        return new ResponseEntity<>(marketDTO, HttpStatus.OK);
    }

    /**
     * 
     * 마켓 정보 수정을 담당하는 핸들러 메서드이다. {@link PutMapping}을 사용하지 않은 이유는, 그러니까 해당 요청의 HTTP
     * 메서드를 PUT이 아닌 POST로 정한 이유는 멀티파트 파일의 업로드가 필요하기 때문이다. 또한 수정 작업임에도 응답 상태 코드가 204-
     * No Content가 아닌 201-Created인 이유는 변경된 정보를 클라이언트에 전달해야 하기 때문이다.
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해
     * {@link Authentication} 타입 오브젝트에 대한 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로
     * 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터 {@code userId}가 전달 된다고
     * 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 않을
     * 것이다.
     * 
     * 
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @ApiOperation(value = "마켓 정보 수정.", response = MarketModifyResponseDTO.class)
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @PostMapping("/auth/market")
    public ResponseEntity<MarketManagementDTO> modifyMarket(UserID userId,
            @Validated MarketModifyRequestDTO marketDTO) {
        int managerId = userId.getUserId();

        marketDTO.setManagerId(managerId);
        MarketManagementDTO modifyMarketDTO = marketService.modifyMarket(marketDTO);

        logger.debug("Modifying market info succeeded. Processing http response building...");

        return new ResponseEntity<>(modifyMarketDTO, HttpStatus.CREATED);
    }
}
