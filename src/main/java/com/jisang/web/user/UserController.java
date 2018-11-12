package com.jisang.web.user;

import static com.jisang.config.code.CodeBook.UserType.MANAGER_USER;
import static com.jisang.config.code.CodeBook.UserType.NORMAL_USER;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.Formatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.jisang.domain.User;
import com.jisang.dto.ErrorDTO;
import com.jisang.dto.user.AuthUserResponseDTO;
import com.jisang.dto.user.SignupDTO;
import com.jisang.dto.user.SignupManagerDTO;
import com.jisang.dto.user.UserModificationDTO;
import com.jisang.security.core.AnonymousUserAuthentication;
import com.jisang.service.user.UserService;
import com.jisang.support.AddressAleadyUsedException;
import com.jisang.support.NoSuchAddressException;
import com.jisang.support.TemporaryPasswordNotificationProvider;
import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;
import com.jisang.support.conversion.PhoneNumberFormatProvider;
import com.jisang.support.conversion.PhoneNumberFormatter;
import com.jisang.support.conversion.UnsupportedLocaleException;
import com.jisang.support.validation.LocaleBasedValidationGroups;
import com.jisang.support.validation.ValidationDelegator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * user관련 핸들러 메서드들이 정의되어 있는 클래스이다. 이 클래스의 /auth 이하의 url에 매핑되는 핸들러 메서드를 보면
 * 
 * 유저 정보를 얻기 위해 사용하는 {@code authentication} 파라미터가 예상과 다른 오브젝트일 경우 올바른 동작이 불가능하기
 * 때문에 이를 검사하는게 안전다하고 생각하여 아래와 같이 구체적인 {@link Authentication}구현 오브젝트에 대한 타입 검사를
 * 수행한다. 시큐리티 단과 웹 어플리케이션 단이 분리되어 있다고 생각했다.
 * 
 * 혼자 만들었기 때문에 어플리케이션이 어떻게 동작할지야 다 알고 있고 혼자 만든게 아니더라도 어떤 타입의
 * {@link Authentication} 구현 오브젝트가 전달 될지는 알 수 있겠지만, 그래도 아래와 같이 타입에 대한 검사를 수행해주는
 * 것이 혹시 모를 변경({@link Authentication} 구현 오브젝트의 변경)에 따른 위험을 방지할 수 있을 것 같다. 안전성을
 * 위해서는 필요하다고 생각되면서도 이렇게 하는 것이 의미가 있나 싶기도 하다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@RestController
@Api(description = "User related API")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MessageSource msgSource;
    @Autowired
    private UserService service;
    @Autowired
    @Qualifier("phoneNumberFormatter")
    private Formatter<String> phoneNumberFormatter;
    @Autowired
    @Qualifier("localeValidationGroupsMapper")
    private Map<Locale, Class<? extends LocaleBasedValidationGroups>> localeValidationMapper;
    @Autowired
    private ValidationDelegator validator;
    @Autowired
    @Qualifier("email")
    private TemporaryPasswordNotificationProvider tempPasswordNotificationProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * {@link User} 도메인에 한해, 이미 존재하는 email에 대한 회원 가입 요청이 발견되었을 때
     * {@link DuplicatedKeyException}이 던져진다. 아래 메서드는 이 예외를 처리한다.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorDTO> onDuplicateUserEmail(DuplicateKeyException ex, WebRequest request) {
        logger.error("An exception occurred associated with duplicated user", ex);

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.CONFLICT.value(),
                msgSource.getMessage("response.exception.DuplicateKeyException.user", null, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.CONFLICT);
    }

    /**
     * 
     * 사용자의 요청으로부터 전달 받은 핸드폰 번호를 사용자의 {@link Locale}에 따른 올바른 국가 코드가 설정되게끔 포맷팅해줘야 한다.
     * 또는 {@link Locale}에 따라 올바른 포맷의 핸드폰 번호가 전달되었는지에 대한 validation도 수행되어야 한다. 아래
     * 메서드가 처리하는 예외 {@link UnsupportedLocaleException}은 이 과정 중 발생한다.
     * 
     */
    @ExceptionHandler(UnsupportedLocaleException.class)
    public ResponseEntity<ErrorDTO> onUnsupportedLocale(UnsupportedLocaleException ex, WebRequest request) {
        logger.error("An exception occurred associated with locale.", ex);

        String topic = ex.getTopic();

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), msgSource
                .getMessage("response.exception.UnsupportedLocaleException" + topic, null, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

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
    public ResponseEntity<ErrorDTO> onNoSuchAddress(AddressAleadyUsedException ex, WebRequest request) {
        logger.error("An exception occurred associated with aleady used address.", ex);

        String address = ex.getAddress();
        String location = ex.getLocation();

        ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),
                msgSource.getMessage("response.exception.AddressAleadyUsedException",
                        new String[] { address, location }, request.getLocale()));

        return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * client에서 01012345678과 같은 핸드폰 번호를 입력하였을 경우 {@link Locale}을 기반으로 핸드폰 번호의 형식을
     * 변경해주는 {@link PhoneNumberFormatter}를 추가한다. 적용 후의 핸드폰 번호는 한국의 경우
     * +8210-1234-5678과 같이 변경된다.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(phoneNumberFormatter, "phone");
    }

    /**
     * 
     * 일반 유저 계정 등록 핸들러 메서드이다.
     * 
     * @param dto
     *            - 파라미터 {@code dto}를 {link RequestBody} 애노테이션으로 받지 않은 이유는
     *            {@code dto} 오브젝트에 {@link Formatter} 적용이 필요했기 때문이다.
     * @param locale
     *            - 이 메서드의 초반부를 보면 추가적으로(spring이 기본적으로 제공하는 핸들러 팔라미터에 대한 binding 후에
     *            수행하는 bean validation에 추가로) validation을 수행함을 알 수 있다. 핸드폰 번호의 유효성
     *            검증은 locale에 따라 달리해야하기 때문에 직접 validation 작업을 수행하는 오브젝트의 메서드를 호출하였다.
     *            {@link Validated} 애노테이션을 통한 검증에는 그룹 지정에 클래스 리터럴만 사용 가능하다.
     * 
     * 
     * @see #initBinder(WebDataBinder)
     *
     */
    @ApiOperation(value = "일반 유저 회원 가입.")
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @PostMapping(path = "/signup/normal")
    public ResponseEntity<Void> signupNormal(@Validated SignupDTO dto, Locale locale) {

        validator.validate(dto, localeValidationMapper.get(locale));

        dto.setRole(NORMAL_USER.getCode());
        service.registerUser(dto);

        logger.debug("Normal user signup succeeded. Processing http response building...");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 
     * 상점 관리자 계정 등록 핸들러 메서드이다.
     * 
     * @param dto
     *            - 파라미터 {@code dto}를 {link RequestBody} 애노테이션으로 받지 않은 이유는
     *            {@code dto} 오브젝트에 {@link Formatter} 적용이 필요했기 때문이다.
     * @param locale
     *            - 이 메서드의 초반부를 보면 추가적으로(spring이 기본적으로 제공하는 핸들러 파라미터에 대한 binding 직후에
     *            수행하는 bean validation에 추가로) bean validation을 수행함을 알 수 있다. 핸드폰 번호의
     *            유효성 검증은 locale에 따라 달리해야하기 때문에 직접 validation 작업을 수행하는 오브젝트의 메서드를
     *            호출하였다. {@link Validated} 애노테이션을 통한 검증에는 그룹 지정에 클래스 리터럴만 사용 가능하다.
     * 
     * 
     * @see #initBinder(WebDataBinder)
     * 
     */
    @ApiOperation(value = "매니저 유저 회원 가입.")
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class) })
    @PostMapping("/signup/manager")
    public ResponseEntity<Void> signupManager(@Validated SignupManagerDTO dto, Locale locale) {
        validator.validate(dto, localeValidationMapper.get(locale));

        dto.setRole(MANAGER_USER.getCode());
        service.registerUser(dto);

        logger.debug("Manager signup succeeded. Processing http response building...");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 
     * 유저 정보(회원 본인) get 요청에 대한 핸들러 메서드이다. 이 메서드를 보면 service 오브젝트로부터 받아온 유저 오브젝트의 핸드폰
     * 번호를 변경함을 알 수 있는데, 이런 로직은 service 오브젝트에서 처리하는 것이 layer 분리의 목적상 옳겠으나
     * {@code locale}에 따라 다른 {@link PhoneNumberFormatProvider} 구현이 사용 되어야해서 이 메서드에서
     * 해당 로직을 수행하였다. service 오브젝트에 {@link Locale}을 전달할 수도 있겠으나 그렇게는 하지 않았다.
     * 
     * 
     * 또한 이 핸들러 메서드는 {@code service} 오브젝트의 {@code #findUseForManagement()} 메서드를
     * 호출하는데, 이와 관련하여 고민을 하였다. 서비스 계층에는 도메인 오브젝트 {@link User}를 반환하는 단 하나의 메서드
     * {@code find}를 두고 두 개의 핸들러 메서드를 정의(하나는 회원 본인에 대한 정보 조회, 다른 하나는 다른 회원에 대한 정보
     * 조회용 핸들러 메서드.)하여 각 핸들러 메서드에서 자신이 반환해야할 DTO 오브젝트로 변환하는 것이다. 컨트롤러에 로직을 최대한 담지
     * 않으려는 생각이었으나 이 방법이 더 괜찮아 보였다. 그러나 위와 같이 하지 않고 서비스 계층에 두 개의 메서드
     * {@code findUser, findUserWithAuth} 를 두어 각 핸들러 메서드에서 자신이 필요로 하는 서비스 계층의 메서드를
     * 호출하게 하였다. 이유는 다음과 같다. 응답 DTO에는 유저 도메인 오브젝트에는 없는 정보(프로퍼티)가 존재할 수 있다. (도메인
     * 오브젝트의 정보를 가공하거나 해서 이 프로퍼티에 값을 채우거나 할 것이다.) 물론 현재야 그런 정보는 없지만 나중을 생각할 때 이런 정보는
     * 충분히 존재할 수 있는데, 이럴 경우 처음 설명한 {@link User} 도메인 오브젝트를 반환하는 서비스 계층 방식은 하는 일이 별로
     * 없다. 단지 DAO에서 반환한 도메인 오브젝트를 반환하기만 하면 될 것이다. 반대로 컨트롤러(아래의 메서드)에 할 일이 매우 많아질
     * 것이다. 정답이야 없겠지만 초기에 스스로 정하기를 비즈니스 로직은 최대한 서비스 오브젝트에 두기로 하였기 때문에 이런 (스스로 정한)정책을
     * 어느 정도 일관되게 지켜가면서 만들고 싶기도 하였다. 이와 같은 이유로 서비스 계층의 메서드를 나누게 되었다.
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
    @ApiOperation(value = "회원 정보 수정 화면 GET.", response = AuthUserResponseDTO.class)
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized") })
    @GetMapping("/auth/user")
    public ResponseEntity<AuthUserResponseDTO> getUserForManagement(UserID userId, Locale locale) {

        AuthUserResponseDTO userDTO = service.findUserForManagement(userId.getUserId());
        userDTO.setPhone(phoneNumberFormatter.print(userDTO.getPhone(), locale));

        logger.debug("Get user info succeeded. Processing http response building...");
        logger.debug("Returnning object : {}", userDTO);

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    /**
     * 
     * 임시 비밀 번호 발급 요청에 대한 핸들러 메서드이다. 임시 비밀 번호 발급을 위해선 seucirty 단에서 JWT token 파싱으로
     * 생성된 유저 정보가 필요하다. (발급할 이메일 또는 핸드폰 번호등을 조회해야 하기 때문) 유저 정보를 얻기 위해 사용하는
     * {@code authentication} 파라미터가 예상과 다른 오브젝트일 경우 올바른 동작이 불가능하기 때문에 이를 검사하는게 안전다하고
     * 생각하여 아래와 같이 구체적인 {@link Authentication}구현 오브젝트에 대한 타입 검사를 수행한다. 시큐리티 단과 웹
     * 어플리케이션 단이 분리되어 있다고 생각했다.
     * 
     * 혼자 만들었기 때문에 어플리케이션이 어떻게 동작할지야 다 알고 있고 혼자 만든게 아니더라도 어떤 타입의
     * {@link Authentication} 구현 오브젝트가 전달 될지는 알 수 있겠지만, 그래도 아래와 같이 타입에 대한 검사를 수행해주는
     * 것이 혹시 모를 변경({@link Authentication} 구현 오브젝트의 변경)에 따른 위험을 방지할 수 있을 것 같다. 안전성을
     * 위해서는 필요하다고 생각되면서도 이렇게 하는 것이 의미가 있나 싶기도 하다.
     * 
     */
    @ApiOperation(value = "임시 비밀번호 발급.")
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 401, message = "Unauthorized") })
    @PatchMapping("/temporary-password")
    public ResponseEntity<Void> issueTemporaryPassword(Authentication authentication, Locale locale) {

        String userEmail = Optional.ofNullable(authentication)
                .filter(auth -> auth instanceof AnonymousUserAuthentication).map(auth -> auth.getName())
                .orElseThrow(() -> {
                    logger.warn("Something is wrong. authentication must be instance of {} but {}",
                            AnonymousUserAuthentication.class, authentication);

                    throw new IllegalStateException("Illegal authentication detected.");
                });

        String temporaryPassword = passwordEncoder
                .encode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));

        UserModificationDTO userDTO = new UserModificationDTO();
        userDTO.setEmail(userEmail);
        userDTO.setPassword(temporaryPassword);

        service.modifyUser(userDTO);
        tempPasswordNotificationProvider.sendTemporaryPassword(userEmail, temporaryPassword);

        logger.debug("Issuing temporary password succeeded. Processing http response building...");
        logger.debug("Returnning object : {}", userDTO);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 
     * 유저 정보 수정을 처리하는 핸들러 메서드이다.
     * 
     * {@link PutMapping}에 {@link RequestBody}를 사용하지 않은 이유는
     * {@link #initBinder(WebDataBinder)} 메서드의 적용을 위해서이며 응답이 204 - No Content가 아닌
     * 201 - Created인 이유는 변경된 정보를 클라이언트에 전달하기 때문이다.
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
    @ApiOperation(value = "회원 정보 수정.", response = UserModificationDTO.class)
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized") })
    @PutMapping("/auth/user")
    public ResponseEntity<UserModificationDTO> modifyUser(@Validated UserModificationDTO modifyDTO, UserID userId,
            Locale locale) {

        validator.validate(modifyDTO, localeValidationMapper.get(locale));

        modifyDTO.setId(userId.getUserId());

        logger.error(modifyDTO.toString());
        UserModificationDTO returnedModifyDTO = service.modifyUser(modifyDTO);

        logger.debug("Modifying user info succeeded. Processing http response building...");
        logger.debug("Returnning object : {}", returnedModifyDTO);

        return new ResponseEntity<>(returnedModifyDTO, HttpStatus.CREATED);
    }

    /**
     * 
     * /login 엔드포인트는 security 단에서 응답을 수행하기 때문에 아래의 핸들러 메서드는 절대 호출되지 않는다. api 문서 작성을
     * 위해 정의한 메서드일 뿐이다.
     * 
     */
    @ApiOperation(value = "로그인.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "유저 이메일.", dataType = "string", paramType = "form", required = true),
            @ApiImplicitParam(name = "password", value = "유저 패스워드.", dataType = "string", paramType = "form", required = true) })
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized") })
    @PostMapping("/login")
    public ResponseEntity<Void> fakeLogin() {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 
     * /logout 엔드포인트는 security 단에서 응답을 수행하기 때문에 아래의 핸들러 메서드는 절대 호출되지 않는다. api 문서 작성을
     * 위해 정의한 메서드일 뿐이다.
     * 
     */
    @ApiOperation(value = "로그아웃.")
    @PostMapping("/logout")
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", paramType = "header", required = true)
    @ApiResponses({ @ApiResponse(code = 201, message = "Created"), @ApiResponse(code = 401, message = "Unauthorized") })
    public ResponseEntity<Void> fakeLogout() {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
