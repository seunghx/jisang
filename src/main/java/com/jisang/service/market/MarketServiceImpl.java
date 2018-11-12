package com.jisang.service.market;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.jisang.domain.Address;
import com.jisang.domain.Market;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyRequestDTO;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyResponseDTO;
import com.jisang.persistence.MapDAO;
import com.jisang.persistence.ManagementDAO;
import com.jisang.persistence.MarketDAO;
import com.jisang.persistence.MultipartDAO;
import com.jisang.support.AddressAleadyUsedException;
import com.jisang.support.ImageOperationProvider;
import com.jisang.support.NoSuchAddressException;

/**
 * 
 * {@link Market} 도메인 관련 비즈니스 로직을 수행하는 서비스 오브젝트이다.
 * 
 * 
 * @author seunghyun
 *
 */
@Service
public class MarketServiceImpl implements MarketService {

    // Instance Fields
    // ==========================================================================================================================
    
    private final Logger logger = LoggerFactory.getLogger(MarketServiceImpl.class);

    @Autowired
    private MarketDAO marketDAO;
    @Autowired
    private ManagementDAO managementDAO;
    @Autowired
    private MultipartDAO multipartDAO;
    @Autowired
    private MapDAO mapDAO;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    @Qualifier("imageTrashCan")
    private List<String> imageTrashCan;

    // Methods 
    // ==========================================================================================================================

    /**
     * 
     * 
     * 현재 지상 어플리케이션 와이어 프레임 상에는 마켓 관리자의 마켓 관리 화면(정보 수정)이 존재한다. 당연히 정보를 수정하려면 기존 정보가
     * 전달되어야 할 것이다. 이 메서드는 이를 목적으로 만들게 되었다. 생각을 해보니 현재 와이어 프레임 상에는 존재하지는 않지만 일반 회원들도
     * 마켓 정보를 볼 수 있어야 할 것이다. 그러나 마켓 관리 화면에서 다루는 정보 중 일반 유저에게 전달되어서는 안되는 정보는 보여서는 안 될
     * 것이다. 이런 이유로 클라이언트에게 전달되는 DTO를 두 가지 로 분리하게 되었다. 하나는
     * {@link MarketModifyResponseDTO}이며 다른 하나는 (와이어 프레임상에 존재하지 않아 만들진 않았음)
     * {@link MarketResponseDTO} 라고 이름 지을 예정이다.
     * 
     * 같은 이유로 핸들러 메서드를 둘로 나누기로 하였다. 마켓 관리자에게 보여줄 정보는 관리자 인증/인가가 필요하므로 첫 번째 핸들러 메서드는
     * /auth/market이라는 url로 접근되며 다른 회원의 정보를 보는 핸들러 메서드는 /market 과 같은 url로 접근될 수 있을
     * 것이다. (현재는 관리자 화면 로직만 존재) 여기서 고민이 생겼다. 마켓 관련 비즈니스 로직을 담는 이 클래스
     * {@link MarketServiceImpl}에서 도메인 오브젝트 {@link Market}을 반환하는 메서드(예를 들어
     * {@code findMarket()}) 하나를 정의하고 각각의 두 경우의 핸들러 메서드에서 이
     * 메서드({@code findMarket()})를 호출한 후 자신의 응답을 위해 필요한 작업을 수행(도메인 오브젝트를 DTO로 변환 혹은
     * 도메인 오브젝트로부터 데이터 가공)하느냐 아니면 아래 구현과 같이 각각의 핸들러 메서드에서 호출 할 서비스 메서드를 정의하여 이
     * 메서드들에서 {@link MarketModifyResponseDTO, MarketResponseDTO}를 반환하게 하여 컨트롤러의 비즈니스
     * 로직을 없애느냐이다. 결국 난 후자의 방법을 선택하였다. 컨트롤러에 비즈니스 로직이 담길 경우 서비스 계층 클래스를 정의한 이유가 사라지기
     * 때문이다. 컨트롤러는 클라이언트로부터 요청 파라미터를 전달 받고 적절한 서비스 메서드를 호출한 후 클라이언트로 응답을 전달하는 역할만
     * 수행하게 하려고 한다.
     * 
     * 클라이언트로 응답되는 DTO 오브젝트에는 도메인 오브젝트에는 존재하지 않는 프로퍼티 등이 필요하게 될 수도 있다. 이런 데이터들은 도메인
     * 오브젝트를 가공하거나 외부의 정보로 부터 가져오거나 하게 될 것이다. 만약 전자의 방법이었다면 이런 상황에서 DTO 구성 로직까지
     * 컨트롤러가 수행하다보니 컨트롤러의 비즈니스 로직 코드가 늘어나며 반대로 서비스 오브젝트는 전혀 하는일이 없이 단지 DAO가 반환하는 도메인
     * 오브젝트만 그대로 전달하게 된다. 가급적 비즈니스 로직은 서비스 계층에 담기로 정하였기 때문에 아래와 같이 서비스 계층 메서드에서
     * {@link MarketModifyResponseDTO}와 같은 구체적 DTO 오브젝트를 반환하는 방법을 선택하였다.
     * 
     * 마켓 정보 반환이라는 점에서는 같은 비즈니스 로직이라고도 생각 할 수 있으나 전달되는 정보와 정보를 받는 대상이 다르기 때문에 마켓 관리
     * 화면, 일반 유저의 마켓 정보 보기 화면은 다른 비즈니스 로직이라고 봐야하며 그렇기 때문에 아래와 같이 메서드를 나누는 것이 더 옳다고
     * 생각한다. (물론 아직 와이어프레임 상에는 존재하지 않으므로 {@code findMarket()}을 구현하지 않았음)
     * 
     * 
     * 아래 메서드를 보면 마켓의 id를 가져오기 위해 {@code managementDAO}의 메서드를 호출함을 알 수 있다. (JWT에 유저의
     * id가 저장되어 있음. 마켓 관리자 화면이 어플리케이션의 메인 기능은 아니기 때문에 이를 위해 마켓 id까지 JWT에 포함하고 싶지는 않아
     * 회원(관리자) id로부터 마켓 id를 찾기로 함.) 두 번의 데이터 베이스 접근이 일어난다는 것인데, 이러지 말고 아래와 같이 JOIN을
     * 이용한 SQL을 짜서 한 번에 마켓 정보를 가져와도 될 것이다.
     * 
     * <pre>
     *	SELECT market_id, market_location, market_name, market_address, market_image 
     *	FROM tbl_markets
     *	JOIN tbl_managements 
     *	ON tbl_markets.market_id = tbl_managements.market_id
     *	WHERE tbl_managements.manager_id = #{managerId}
     * </pre>
     * 
     * 그러나 이렇게 하지 않은 이유는 다음과 같다. 마켓 정보 조회 로직은 위에도 말했지만 일반 유저가 마켓 정보를 볼 때도 필요한 정보이다.
     * 그런데 SQL을 위와 같이 짜버릴 경우 이는 일반 유저의 마켓 정보 조회시에는 사용될 수 없다. 일반 유저가 이용하는 클라이언트
     * 어플리케이션 입장에서 알고 있는 정보는 마켓의 id이지 매니저의 id가 아니기 때문이다. 그러므로 DAO의 메서드는 두 개가 될 것이다.
     * 하나는 위와 같은 관리자 아이디를 전달한 JOIN 방법과 다른 하나는 마켓 아이디를 이용한 조회용 데이터 엑세스 메서드이다. 재사용성에
     * 좋지 않다고 생각하였다. 그래서 DAO가 수행하는 쿼리는 마켓 아이디를 받아 정보를 조회하는 하나의 SQL로 통일하였다. 그렇기 때문에
     * 아래의 메서드는 {@code managementDAO}로부터 마켓 아이디를 가져온다.
     * 
     * 
     * 또한 아래를 보면 {@code managementDAO}가 null을 반환하는지를 검사함을 알 수 있는데, 이와는 다르게
     * {@code marketDAO}에 대해서는 이런 검사를 하지 않는다. 혹시 모를 상황(예를 들어, 데이터베이스상에 외래키 제약 조건이
     * 삭제됨.)을 대비하면 이 부분에 대해서는 검사를 진행하는 게 좋긴 하나 {@code marketDAO} 의 경우 (외래키 제약 조건이
     * 삭제되지 않았다면) 마켓 정보가 데이터베이스로부터 삭제되었다면 위의 {@code managementDAO}에서 마켓 아이디를 반환할 수가
     * 없기 때문에(ON DELETE CASCADE에 의해) 검사를 하지 않았다.
     * 
     */
    @Override
    @Transactional
    public MarketModifyResponseDTO findMarketForManagement(int managerId) {
        int marketId = validateManager(managerId);

        logger.debug("Finding market id succeeded.  market id : {}", marketId);

        MarketModifyResponseDTO resDTO = new MarketModifyResponseDTO();

        modelMapper.map(marketDAO.read(marketId), resDTO);
        modelMapper.map(mapDAO.readAddressByMarketId(marketId), resDTO);

        return resDTO;
    }

    /**
     * 
     * 마켓 정보를 업데이트한다. {@link MarketModifyResponseDTO}를 반환함을 알 수 있는데 클라이언트에서 수정 응답 확인
     * 후 다시 GET 요청을 하는 방법이 있겠으나 화면 응답에는 이 방법이 빠를 것이므로 위와 같이 하기로 하였다.
     * 
     * 컨트롤러 메서드에 오브젝트 바인딩이 성공해야 이 메서드가 호출될 것이기 때문에 null 검사는 필요 없는 부분일 수 있다. 그러나 컨트롤러
     * 코드나 그 외 AOP 적용 등의 과정의 실수로 인해 null 값이 전달될 경우를 위해
     * {@code Objects.requireNonNull}을 호출하였다.
     * 
     * 예외가 발생하면 S3와 같은 클라우드 스토리지에 업로드 된 이미지(업로드 된 경우)를 삭제함을 알 수 있다. 삭제 작업 도중 에외가
     * 발생하여 삭제가 되지 않는다면 후에 스케줄러를 이용한 삭제가 적용되도록 {@code imageTrashCan}에 담는다.
     * 
     * 이 클래스의 다른 메서드와 마찬가지로 로깅은 {@link MarketServiceAspect}에 의해 수행된다.
     * 
     */
    @Override
    @Transactional
    public MarketModifyResponseDTO modifyMarket(MarketModifyRequestDTO marketDTO) {

        Objects.requireNonNull(marketDTO, "Null value argument marketDTO detected while trying to modify market info.");

        int marketId = validateManager(
                            Objects.requireNonNull(marketDTO, "Illegal state. marketDTO is null").getManagerId());

        logger.debug("Finding market id succeeded. market id : {}", marketId);

        marketDTO.setId(marketId);
        updateAddress(marketDTO);

        MultipartFile file = null;
        String imageUrl = null;

        try {
            file = marketDTO.getImageFile();

            if (!Objects.isNull(file)) {
                ImageOperationProvider.validateImage(file.getOriginalFilename());
                deleteOlderImages(marketId);
                imageUrl = multipartDAO.upload(file).get(0);
            }

            Market market = modelMapper.map(marketDTO, Market.class);
            market.setImageUrl(imageUrl);

            marketDAO.update(market);

            MarketModifyResponseDTO resDTO = modelMapper.map(marketDTO, MarketModifyResponseDTO.class);
            resDTO.setImageUrl(imageUrl);
            return resDTO;

        } catch (RuntimeException e) {

            if (!Objects.isNull(imageUrl)) {
                logger.info("Deleting representing image {} from cloud storage.", imageUrl);

                try {
                    multipartDAO.delete(imageUrl);
                } catch (AmazonClientException ex) {
                    logger.error("Despite of retryed deletion, Deleting images from storage failed. Failed image : {}.",
                            imageUrl);

                    logger.info(
                            "Putting failed image to trash can... Images in trash can will be deleted by scheduler.");
                    imageTrashCan.add(imageUrl);

                    logger.info(
                            "Exception associated with failed image deletion is not be propagated anymore. Exception : ",
                            e);
                }
            }
            throw e;
        }
    }

    private int validateManager(int managerId) {
        logger.debug("Starting to validate manager id : {}", managerId);

        return Optional.ofNullable(managementDAO.readMarketId(managerId)).orElseThrow(() -> {
            logger.error("Fatal error:::no mapping for manager to market");
            logger.error("Fatal error:::row, mapping manager id:{} to market id removed in tbl_managements.");

            throw new IllegalStateException("No mapping for manager to market.");
        });
    }

    /**
     * 
     * AWS S3와 같은 스토리지로부터 이미지를 삭제한다. 이 메서드의 catch 절을 보면 예외를 잡고 더 이상 던지지 않음을 알 수 있는데
     * 어플리케이션 관리자가 S3와 같은 스토리지의 데이터를 직접 삭제하도록 하고 사용자에게는 서버 에러 등의 메세지 없이 요청이 잘 처리 된
     * 것과 같이 동작되게 하기 위함이다. 이렇게 동작되게 하여도 사용자에게 잘못된 이미지 정보가 전달될 일이 없는데 이유는, S3 상에 저장되어
     * 있는 이미지 파일의 url이 RDB에 담겨있고 서버는 클라이언트에게 이 url을 전달하기 때문이다. 즉 사용자는 S3에 새로 update된
     * 이미지 파일에 대한 url만을 전달 받을 것이다.
     * 
     * 삭제 연산이 실패할 경우 일괄 삭제를 위해 {@code imageTrashCan}에 후에 삭제되어야 할 이미지를 추가함을 알 수 있다.
     * DAO 메서드에서 이와 같은 작업을 해도 되나 그렇게 하지 않은 이유는 DAO 메서드가 실패할 경우 실패한 메서드가 retry 되도록 하기
     * 위함이다.
     *
     */
    private void deleteOlderImages(int marketId) {

        logger.debug("Deleting older market image from image storage started.");

        Optional.ofNullable(marketDAO.read(marketId).getImageUrl()).ifPresent(img -> {
            try {
                multipartDAO.delete(img);

                logger.debug("Deleting older market image from image storage succeeded.");

            } catch (AmazonClientException e) {

                logger.error("Despite of retryed call, Deleting market image from storage failed. Failed image : {}.",
                        img);
                logger.info("Putting failed image to trash can... Images in trash can will be deleted by scheduler.");

                imageTrashCan.add(img);

                logger.info(
                        "Exception associated with failed image deletion is not be propagated anymore. Exception : ",
                        e);
            }
        });
    }

    private void updateAddress(MarketModifyRequestDTO modifyDTO) {

        /*
         * 아래 if절을 통한 검사 부분을 컨트롤러 메서드 파라미터 바인딩 시에 DAO를 이용하여 검사하게 하는 방법도 있겠다. 무엇이 더 좋은
         * 방법인지 모르겠는데 그 이유는 컨트롤러 메서드 파라미터 바인딩에 성공한 정보가 컨트롤러나 그 외 다른 오브젝트를 거치는 과정 중 변경될
         * 수도 있기 때문이다. 과한 걱정인가 싶기도 하다. 오동작하는 클래스가 어플리케이션에 최종적으로 추가되지는 않을 것이기 때문이다.
         */
        Address address = mapDAO.readAddressByAddressIdAndLocation(modifyDTO.getAddress(), modifyDTO.getLocation());

        if (address == null) {

            logger.info("Received non-existing address id.");

            throw new NoSuchAddressException("Received address information does not exist."
                                            , modifyDTO.getAddress(), modifyDTO.getLocation());
        } else if (address.getMarket() != null && address.getMarket().getId() != modifyDTO.getId()) {

            logger.info("Received address is aleady used by other market.");

            throw new AddressAleadyUsedException("Received address is used aleady."
                                               , modifyDTO.getAddress(), modifyDTO.getLocation());
        }

        logger.debug(
                "Validating address related info before updating address info succeeded. Now starting to update address info.");

        mapDAO.updateAddressMarketIdByOlderMarketId(modifyDTO.getId(), null);
        mapDAO.updateAddressMarketId(modifyDTO.getAddress(), modifyDTO.getLocation(), modifyDTO.getId());
    }
}
