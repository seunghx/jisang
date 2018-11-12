package com.jisang.web.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.jisang.dto.product.ProductListViewDTO.ManagementProductListView;
import com.jisang.dto.product.ProductListViewDTO.ProductListView;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyRequestDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyResponseDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductRegisterRequestDTO;
import com.jisang.dto.ErrorDTO;
import com.jisang.dto.product.ProductListViewConfigData;
import com.jisang.dto.product.ProductShoppingDetailedViewDTO;
import com.jisang.service.product.IllegalManagerAccessDetectedException;
import com.jisang.service.product.ProductService;
import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;
import com.jisang.support.validation.ProductListViewConstraintSequence;
import com.jisang.support.validation.ProductModifying;
import com.jisang.web.GlobalExceptionHandler;
import com.jisang.web.LoggingHandlerInterceptor;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import static com.jisang.config.code.CodeBook.MallLocation;

import java.time.LocalDate;
import java.util.List;

/**
 * 
 * 지상 어플리케이션을 통해 판매되는 상품에 대한 컨트롤러 클래스이다. 다른 컨트롤러 클래스와 마찬가지로 흐름의 시작과 끝을 알리는 등의 로깅
 * 작업은 {@link LoggingHandlerInterceptor}에서 수행하며 (컨트롤러 공통)예외 처리는
 * {@link GlobalExceptionHandler}에서 처리한다.
 * 
 * @author leeseunghyun
 *
 */
@RestController
@Api(description = "Product related API")
public class ProductController {
    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * 
     * 타 마켓 관리자 계정으로 부터 해당 관리자가 관리하지 않는 상품(다른 마켓의 상품)에 대한 수정 요청이 발견 되었을 경우
     * {@link IllegalManagerAccessDetectedException} 이 던져진다. 아래 메서드에서 이 예외를 처리한다.
     *
     */
    @ExceptionHandler(IllegalManagerAccessDetectedException.class)
    public ResponseEntity<Void> onIllegalManagerAccessDetected(IllegalManagerAccessDetectedException ex,
            WebRequest request) {
        logger.error("An exception occurred associated with illegal access to product management.", ex);
        logger.error(
                "Illegal access detected. Someone tried to access another market's product id : {} with account id {}",
                ex.getProductId(), ex.getManagerId());

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    /**
     * 상품 정보 상세 조회 요청을 처리하는 핸들러 메서드이다.
     */
    @ApiOperation(value = "상품 상세 정보 조회", response = ProductShoppingDetailedViewDTO.class)
    @ApiImplicitParam(name = "productId", value = "조회 될 상품 id.", dataType = "int", paramType = "path", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK") })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductShoppingDetailedViewDTO> getProduct(@PathVariable("productId") int productId) {

        ProductShoppingDetailedViewDTO productDTO = productService.findProductForShopping(productId);

        logger.debug("Finding product with id {} succeeded.", productId);

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    /**
     * 특정 마켓에 대한 상품 목록 조회 요청을 처리하는 핸들러 메서드이다.
     */
    @ApiOperation(value = "특정 마켓 상품 목록 조회.", response = ProductListView.class)
    @ApiImplicitParam(name = "marketId", value = "상품 목록에 대한 마켓 id.", dataType = "int", paramType = "path", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @GetMapping("/market/{marketId}/products")
    public ResponseEntity<ProductListView> getMarketProductList(
            @Validated(ProductListViewConstraintSequence.class) ProductListViewConfigData viewConfig,
            @PathVariable("marketId") Integer marketId) {
        viewConfig.setMarketId(marketId);

        ProductListView dto = productService.findProductList(viewConfig);

        logger.debug("Finding product list with market id {} succeeded.", marketId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * 특정 지하상가 내의 상품 목록 조회를 처리하는 핸들러 메서드이다.
     */
    @ApiOperation(value = "지하상가별 상품 목록 조회.", response = ProductListView.class)
    @ApiImplicitParam(name = "mallLocation", value = "지하상가 지역 코드. 예)강남 : '11'.", dataType = "string", paramType = "path", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @GetMapping("/mall/{mallLocation}/products")
    public ResponseEntity<ProductListView> getMallproductList(
            @Validated(ProductListViewConstraintSequence.class) ProductListViewConfigData viewConfig,
            @PathVariable("mallLocation") String mallLocation) {
        viewConfig.setMallLocation(mallLocation);
        ProductListView dto = productService.findProductList(viewConfig);

        if (logger.isDebugEnabled()) {
            logger.debug("Finding product list with mall location code : {} succeeded.",
                    MallLocation.fromString(mallLocation));
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * 
     * 마켓 관리자 화면에서의 상품 등록시 호출되는 핸들러 메서드이다.
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
    @ApiOperation(value = "상품 등록")
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", dataType = "string", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @PostMapping("/auth/product")
    public ResponseEntity<Void> postProduct(@Validated ProductRegisterRequestDTO productDTO, UserID userId) {

        logger.debug("Starting to call {} for registering product.", productService);

        productService.registerProduct(userId.getUserId(), productDTO);

        logger.debug("Registering product succeeded.");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 
     * 상품 정보 관리(수정)을 위한 상품 정보 조회
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
    @ApiOperation(value = "상품 관리 화면에서의 상품 상세 조회", response = ProductModifyResponseDTO.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "productId", value = "수정 화면을 위해 조회 될 상품 id.", dataType = "int", paramType = "path", required = true) })
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden") })
    @GetMapping("/auth/product/{productId}")
    public ResponseEntity<ProductModifyResponseDTO> getProductForManagement(@PathVariable("productId") int productId,
            UserID userId) {

        logger.debug("Starting to call {} for find product.", productService);

        ProductModifyResponseDTO dto = productService.findProductForModifying(userId.getUserId(), productId);

        logger.debug("Findeing product info id : {} for management succeeded.", productId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * 
     * 일반적으로 수정 요청에는 PUT 메서드가 이용되나 {@link MultipartFile}를 다른 정보와 함께 form 데이터로 전달되도록
     * POST 요청을 받도록 정하였다. 찾아보기로는 {@link MultipartResolver}를 custom하는 방법도 있는듯하나 간단히
     * {@link @PostMapping}로 처리하였다.
     *
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
    @ApiOperation(value = "상품 정보 수정.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "productId", value = "수정될 상품 id.", dataType = "int", paramType = "path", required = true) })
    @ApiResponses({ @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @PostMapping("/auth/product/{productId}")
    public ResponseEntity<Void> modifyProduct(@Validated(ProductModifying.class) ProductModifyRequestDTO productDTO,
            @PathVariable("productId") int productId, UserID userId) {

        productDTO.setId(productId);

        logger.debug("Starting to call {} for modifing product id : {}.", productService, productId);

        productService.modifyProduct(userId.getUserId(), productDTO);

        logger.debug("Modifying product info with product id : {} succeeded.", productId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 
     * 상품 삭제 요청을 처리하는 핸들러 메서드이다.
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
    @ApiOperation(value = "상품 삭제.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "productId", value = "삭제될 상품 id.", dataType = "int", paramType = "path", required = true) })
    @ApiResponses({ @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden") })
    @DeleteMapping("/auth/product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") int productId, UserID userId) {

        logger.debug("Starting to call {} for deleting product id : {}.", productService, productId);

        productService.deleteProduct(userId.getUserId(), productId);

        logger.debug("Deleting product info succeeded.");

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 
     * 마켓 관리자 화면에서의 특정 등록 날짜에 해당하는 상품 조회시 호출되는 핸들러 메서드이다.
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
    @ApiOperation(value = "특정 날짜에 등록된 상품 목록. (와이어 프레임 상에서 특정 날짜 선택할 때 나오는 상품 목록 화면)", response = ManagementProductListView.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "uploadDate", value = "이 전 화면('/auth/product/listAll' 응답 화면)에 각 그룹 별로 전달 된 날짜 정보. yyyy-MM-dd 형식", dataType = "string", paramType = "query", required = true) })
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @GetMapping("/auth/product/list")
    public ResponseEntity<ManagementProductListView> getProductListByDate(UserID userId,
            @RequestParam("uploadDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd") LocalDate uploadDate) {

        logger.debug("Starting to call {} for getting product list by specified date {}", productService, uploadDate);

        ManagementProductListView mplv = productService.findProductListByDate(userId.getUserId(), uploadDate);

        logger.debug("Getting product list by date succeeded.");

        return new ResponseEntity<>(mplv, HttpStatus.OK);
    }

    /**
     * 
     * 마켓 관리자 화면에서의 전체 상품 목록 조회시 호출되는 핸들러 메서드이다.
     * 
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
    @ApiOperation(value = "등록 날짜로 그룹핑 된 마켓 내 전체 상품 리스트. (와이어 프레임 상 날짜 별로 그룹핑 된 전체 상품 목록 화면.)", response = Iterable.class)
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @GetMapping("/auth/product/listAll")
    public ResponseEntity<List<ManagementProductListView>> getAllProductListForManagement(UserID userId) {

        logger.debug("Starting to call {} for getting product list grouped by date.", productService);

        List<ManagementProductListView> productList = productService.findProductListForManagement(userId.getUserId());

        logger.debug("Getting product list grouped by date succeeded.");

        return new ResponseEntity<>(productList, HttpStatus.OK);

    }
}
