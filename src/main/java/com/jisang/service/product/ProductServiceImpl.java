package com.jisang.service.product;

import static com.jisang.config.code.CodeBook.ProductListViewType.BEST;
import static com.jisang.config.code.CodeBook.ProductListViewType.PAGE;
import static com.jisang.config.code.CodeBook.ProductListViewType.RECOMMENDED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.jisang.domain.HashTag;
import com.jisang.domain.Market;
import com.jisang.domain.Product;
import com.jisang.dto.product.ProductListViewConfigData;
import com.jisang.dto.product.ProductListViewDTO.ManagementProductListView;
import com.jisang.dto.product.ProductListViewDTO.PagenationProductListView;
import com.jisang.dto.product.ProductListViewDTO.ProductListView;
import com.jisang.dto.product.ProductListViewDTO.ProductListViewEntity;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyRequestDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyResponseDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductMutationDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductRegisterRequestDTO;
import com.jisang.dto.product.ProductShoppingDetailedViewDTO;
import com.jisang.dto.product.criteria.BestViewCriteria;
import com.jisang.dto.product.criteria.BestViewCriteria.BestViewCriteriaBuilder;
import com.jisang.dto.product.criteria.PageViewCriteria;
import com.jisang.dto.product.criteria.PageViewCriteria.PageViewCriteriaBuilder;
import com.jisang.dto.product.criteria.RecommendedViewCriteria;
import com.jisang.dto.product.criteria.RecommendedViewCriteria.RecommendedViewCriteriaBuilder;
import com.jisang.persistence.HashTagDAO;
import com.jisang.persistence.ManagementDAO;
import com.jisang.persistence.MarketDAO;
import com.jisang.persistence.MultipartDAO;
import com.jisang.persistence.ProductDAO;
import com.jisang.support.validation.ProductListViewValidator;
import com.jisang.web.product.ProductController;

/**
 *
 * {@link Product} 도메인 관련 비즈니스 로직을 수행하는 서비스 오브젝트이다.
 *
 * @author leeseunghyun
 *
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductDAO productDAO;
    @Autowired
    private ManagementDAO managementDAO;
    @Autowired
    private MarketDAO marketDAO;
    @Autowired
    private MultipartDAO multipartDAO;
    @Autowired
    private HashTagDAO hashTagDAO;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    @Qualifier("imageTrashCan")
    private List<String> imageTrashCan;
    @Autowired
    @Qualifier("hashtagTrashCan")
    private List<Integer> hashtagTrashCan;

    @Value("${view.best.past-hit-weight}")
    private double pastHitWeight;
    @Value("${view.best.current-hit-weight}")
    private double currentHitWeight;

    // Methods related to product deletion
    // ==========================================================================================================================

    /**
     *
     * update를 수행하기 전 호출되는 메서드로 전달 받은 파라미터 {@code productId}에 해당하는 해시태그들을 삭제한다.
     *
     * 현재 지상어플리케이션은 해시태그 검색어 미리보기 기능은 엘라스틱서치를 이용해 수행되며 해시 태그를 이용한 상품 검색은 RDB를 이용한다.
     * 즉 해시태그 정보가 현재 엘라스틱 서치 뿐만아니라 RDB에도 존재하는 상태이다. 이렇게 하게 된 이유는 ({@link Product} 관련
     * 정보를 RDB에서 엘라스틱서치로 이동시킬지를 고민하는 단계이다보니) {@link Product} 정보가 아직 RDB에 존재하기 때문이다.
     * 해시태그를 이용한 상품 검색 기능을 위해 {@link Product} 정보를 기존 RDB에서 엘라스틱서치로 이동시켰을 경우
     * {@link Product}와 연관된 도메인 정보 (예를 들어 {@link Market}) 정보도 엘라스틱서치에 두는 것이 나을
     * 것이다.(현재 사용 중인 엘라스틱서치 5.5(포함 5.x)버전 은 _parent, _child 필드에 부모 혹은 자식 도큐먼트를 지정할 수
     * 있다.) 만약 {@link Market} 또한 엘라스틱서치에 저장하게 된다면 이와 연관된 매니저 정보 {@link User} 또한
     * 엘라스틱서치로 이동시켜야만 할 것 같다. 그러나 엘라스틱서치는 ROLLBACK과 같은 트랜잭션 지원을 하지 않기 때문에 예를 들어, 회원
     * 가입 기능의 비즈니스 로직(현재 구현은 RDB의 tbl_users, tbl_markets, tbl_managements에 데이터를 각각
     * 저장한다.)과 같은 동작을 수행하다가 예외가 발생할 경우 올바르지 않은 상태의 데이터가 존재해버릴 수 있다.
     *
     *
     * {@link Product} 관련 정보가 RDB에 존재하는 상황에서 해시태그를 이용한 상품 검색시 엘라스틱서치로의 검색 1번, 그리고
     * 엘라스틱서치로부터 얻어낸 상품 id를 이용해 상품 정보를 RDB로부터 가져오는데 n번, 총 n+1번의 읽기 연산이 발생하기 때문에
     * {@link Product}정보가 RDB에 존재하는 동안은 1번의 DB 접근을 위해 상품에 대한 해시태그 목록 또한 RDB에 유지할 수
     * 밖에 없었다.(해시태그는 {@link Product} 도메인을 구성하는 정보인데 이를 다른 생각한다.)
     *
     * 그러다보니 이 메서드에서는 RDB와 엘라스틱서치 모두에 삭제 연산을 호출한다.
     *
     */
    private void deleteOlderHashTags(int productId) {
        logger.debug("Starting to delete older hashtags.");

        productDAO.deleteHashTags(productId);

        deleteOlderHashTagsFromES(productId);

        logger.debug("Deleting older hashtags succeeded.");
    }

    /**
     *
     * {@link #deleteOlderImages(String...)} 메서드와 달리 이 메서드에서 엘라스틱서치에 저장된 해시태그를 삭제하는
     * 도중 예외가 발생할 경우 이 메서드는 예외를 던진다. 이미지의 경우 삭제에 실패해도 RDB에 저장되어 있는 이미지 url만 잘 삭제되었다면
     * 사용자에게 잘못된 이미지(삭제하려다 실패한 이미지)가 보여지는 일이 발생하지 않는다. 그러나 해시태그의 경우는 클라이언트에 url이
     * 전달되는 것이 아니라 해시태그 그 자체가 전달되는 것이기 때문에 비록 검색어 미리보기에서나 사용되는 정보이긴 하나 의미없는 정보가
     * 사용자에게 전달된다. 스케줄러에 의해 삭제되기 전까지는 그런 의미없는 정보가 다른 사용자들에게 약간이라도 불편을 줄 수 있는 것이기 때문에
     * 이 메서드를 호출하게 한 (HTTP)요청을 수행한 사용자에게 동작 실패를 알려 사용자가 다시 요청하게끔 유도하는 것이 그나마 더 낫다고
     * 생각했다. 혹여나 재시도 된 요청에서는 해시태그 삭제가 제대로 될 수도 있기 때문이다.
     *
     */
    private void deleteOlderHashTagsFromES(int productId) {
        logger.debug("Starting to delete older hashtags from elasticsearch.");

        try {
            hashTagDAO.deleteAllByProductId(productId);

            logger.debug("Deleting older hashtags from elasticsearch succeeded.");

        } catch (ElasticsearchException e) {
            logger.error(
                    "Despite of retryed call, Deleting hashtag from elasticsearch with product id : {} failed. : {}.",
                    productId);
            logger.info("Putting failed image to trash can... Images in trash can will be deleted by scheduler.");

            hashtagTrashCan.add(productId);

            throw e;
        }
    }

    /**
     * update를 수행하기 전 호출되는 메서드로 RDB에 저장되어 AWS S3와 같은 스토리지 상의 실제 상품 이미지 파일을 가리키는 url을
     * 삭제한다.
     */
    private void deleteOlderImageUrls(int productId) {
        logger.debug("Starting to delete older image urls from DB.");

        productDAO.deleteImages(productId);

        logger.debug("Deleting older image urls from DB succeeded.");
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
    private void deleteOlderImages(String... imageUrls) {

        logger.debug("Deleting older images from image storage started.");

        Objects.requireNonNull(imageUrls,
                "Null value argument imageUrls detected while trying to delete images from storage.");

        try {
            multipartDAO.delete(imageUrls);

            logger.debug("Deleting older images from image storage succeeded.");

        } catch (AmazonClientException e) {

            logger.error("Despite of retryed call, Deleting images from storage failed. Failed image : {}.",
                    Arrays.asList(imageUrls));
            logger.info("Putting failed image to trash can... Images in trash can will be deleted by scheduler.");

            imageTrashCan.addAll(Arrays.asList(imageUrls));

            logger.info("Exception associated with failed image deletion is not be propagated anymore. Exception : ",
                    e);
        }
    }

    /**
     *
     * 마켓의 상품을 삭제하는 서비스 메서드이다.
     *
     */
    @Override
    public void deleteProduct(int managerId, int productId) {
        if (managementDAO.readMarketIdWithProductId(managerId, productId) == null) {
            logger.error("Fatal:::Illegal access detected with Account id : {}.", managerId);
            logger.error("Fatal:::Account id {} tried to access another market's product. product id:{}", managerId,
                    productId);

            throw new IllegalManagerAccessDetectedException("Illegal access to product management detected.", managerId,
                    productId);
        }

        Product product = productDAO.read(productId);

        List<String> imageUrls = product.getImageUrls();
        String representingImageUrl = product.getRepresentingImageUrl();

        deleteOlderHashTagsFromES(productId);

        deleteOlderImages(imageUrls.toArray(new String[imageUrls.size()]));
        deleteOlderImages(representingImageUrl);

        productDAO.delete(productId);
    }

    // Methods related to product finding
    // ==========================================================================================================================

    /**
     * 지상 어플리케이션의 쇼핑 화면 중 특정 상품에 대한 쇼핑 화면(와이어 프레임 상에서 특정 상품 정보 화면)의 구성을 위한 로직을 수행한다.
     */
    @Override
    public ProductShoppingDetailedViewDTO findProductForShopping(int productId) {
        Product product = productDAO.read(productId);
        Market market = marketDAO.read(product.getMarketId());

        logger.debug("Loading domain object to form product shopping view data succeeded.");

        ProductShoppingDetailedViewDTO productDTO = modelMapper.map(product, ProductShoppingDetailedViewDTO.class);
        modelMapper.map(market, productDTO);

        productDAO.addHit(productId);

        return productDTO;
    }

    /**
     *
     * 지상 어플리케이션 화면의 대부분을 차지하는 (여러 유형의)상품 목록 화면에 필요한 데이터를 처리하는 비즈니스 메서드이다. 실제 처리는
     * {@link #findProductListPage}, {@link #findProductListBest},
     * {@link #findProductListRecommended}에 위임한다.
     *
     * @throws IllegalStateException
     *             {@link com.jisang.config.code.CodeBook.ViewType}에 해당되지 않은 값이
     *             {@code viewConfig.viewType} 프로퍼티로 전달되었을 경우 발생한다. 이 예외가 던져지는 이유는
     *             컨틀롤러 단에서의 바인딩 시의 bean validation에 통과된 값이 잘못 되었음은 컨트롤러 메서드 등 어딘가의
     *             코드가 잘못되거나 했을 것이기 때문이다.
     *
     */
    @Override
    public ProductListView findProductList(ProductListViewConfigData viewConfig) {
        Objects.requireNonNull(viewConfig,
                "Null value argument viewConfig detected while trying to find product list.");

        if (BEST.equalsByCode(viewConfig.getViewType())) {
            logger.debug("Resolved view type : {}", BEST);
            return findProductListBest(viewConfig);
        }

        else if (PAGE.equalsByCode(viewConfig.getViewType())) {
            logger.debug("Resolved view type : {}", PAGE);
            return findProductListPage(viewConfig);
        }

        else if (RECOMMENDED.equalsByCode(viewConfig.getViewType())) {
            logger.debug("Resolved view type : {}", RECOMMENDED);
            return findProductListRecommended(viewConfig);
        }

        else {
            logger.error("Invalid view type detected. Invalid view type : {}", viewConfig.getViewType());
            logger.error("It's server code's fault. Finding illegal code in {} or {} required.",
                    ProductListViewValidator.class, ProductController.class);

            throw new IllegalStateException("Invalid view type detected in " + this.getClass());
        }
    }

    /**
     * 현재의 지상 어플리케이션의 와이어 프레임 상으로는, 와이어 프레임 상의 상품 목록 화면의 대부분을 차지하는 기본 상품 목록 화면 구성에
     * 필요한 동작을 수행한다. 현재 와이어프레임만 고려하였을 경우 단지 상품 목록 전체를 반환하게 해도 되나 상품 목록이 많아질 때
     * 페이지네이션이 무조건 필요할 것이라고 생각되어 {@link PageViewCriteria} 를 이용하여 상품목록을 반환하도록 하였다.
     */
    private PagenationProductListView findProductListPage(ProductListViewConfigData viewConfig) {

        logger.debug("Starting to find product list with view type {}.", PAGE);

        int totalCount = productDAO.readProductTotalCount(viewConfig);

        PageViewCriteria criteria = 
                new PageViewCriteriaBuilder(viewConfig.getMarketId(), viewConfig.getMallLocation(),
                                            viewConfig.getCategory(), totalCount).pageIndex(viewConfig.getPageIndex())
                        .perPageCnt(viewConfig.getPerPageCnt())
                        .searchKeyword(viewConfig.getSearchCriteria().getKeyword())
                        .build();

        List<Product> productList = productDAO.readListPage(criteria);

        logger.debug("Finding product list with view type {} succeeded.", PAGE);

        logger.debug("Starting to return {} type object.", PagenationProductListView.class);

        PagenationProductListView viewDTO = new PagenationProductListView();

        viewDTO.setProductEntityList(productList.stream()
                                                .map(product -> modelMapper.map(product, ProductListViewEntity.class))
                                                .collect(Collectors.toList()));

        viewDTO.setCurrentPageIdx(criteria.getPageIndex());
        viewDTO.setEndPageIdx(criteria.getEndPage());

        return viewDTO;
    }

    /**
     * 조회수를 기반으로 클라이언트에서 요청한 개수 만큼의 best 상품 목록을 반환한다.
     */
    private ProductListView findProductListBest(ProductListViewConfigData viewConfig) {

        logger.debug("Starting to find product list with view type {}.", BEST);

        BestViewCriteria criteria = 
                new BestViewCriteriaBuilder(viewConfig.getMarketId(), viewConfig.getMallLocation(),
                                            viewConfig.getCategory(), viewConfig.getBestViewCnt())
                        .pastHitWeight(pastHitWeight)
                        .currentHitWeight(currentHitWeight).searchKeyword(viewConfig.getSearchCriteria().getKeyword())
                        .build();

        List<Product> productList = productDAO.readListBest(criteria);

        logger.debug("Finding product list with view type {} succeeded.", BEST);

        logger.debug("Starting to return {} type object.", ProductListView.class);

        ProductListView viewDTO = new ProductListView();
        viewDTO.setProductEntityList(productList.stream()
                                                .map(product -> modelMapper.map(product, ProductListViewEntity.class))
                                                .collect(Collectors.toList()));
        return viewDTO;
    }

    /**
     * 지상 어플리케이션의 와이어 프레임 상의 MD's pick이라고 지정된 화면과 또한 마켓 쇼핑 화면 상의 best 라고 표시된 상품
     * 목록(와이어 프레임상에는 왜 best라고 되어 있는지 모르겠으나 프로젝트 당시 프로젝트 매니저의 요구사항은 마켓 관리자가 직접 지정하는
     * 추천 상품이었으며 실제 구현도 그렇게 되어 있다. 상품 수정화면에서 추천 상품을 best라는 이름으로 마켓 관리자가 지정할 수 있게
     * 되어있다. - 상품 조회수를 기반으로 best 상품을 출력하는 화면의 best와 다르다.)화면 구성에 필요한 동작을 수행한다. 클라이언트가
     * 요청한 개수 만큼의 추천 상품 목록을 반환한다.
     */
    private ProductListView findProductListRecommended(ProductListViewConfigData viewConfig) {

        logger.debug("Starting to find product list with view type {}.", RECOMMENDED);
        
        RecommendedViewCriteria criteria = 
                new RecommendedViewCriteriaBuilder(viewConfig.getMarketId(), viewConfig.getMallLocation(), 
                                                   viewConfig.getCategory(), viewConfig.getRecommendedViewCnt())
                        .searchKeyword(viewConfig.getSearchCriteria().getKeyword())
                        .build();

        List<Product> productList = productDAO.readListRecommended(criteria);

        logger.debug("Finding product list with view type {} succeeded.", RECOMMENDED);

        logger.debug("Starting to return {} type object.", ProductListView.class);

        ProductListView viewDTO = new ProductListView();
        viewDTO.setProductEntityList(productList.stream()
                                                .map(product -> modelMapper.map(product, ProductListViewEntity.class))
                                                .collect(Collectors.toList()));
        return viewDTO;
    }

    /**
     * 지상 어플리케이션의 와이어프레임 상의 마켓 관리 화면에을 보면 상품 등록 일자 별로 그룹핑 된 상품 목록 화면과 해당 화면에서 특정 일자에
     * 대한 그룹을 선택할 경우 선택된 그룹의 상품 목록을 출력하는 화면이 존재한다. 이 메서드는 전자의 화면 구성에 필요한 동작을 수행한다.
     */
    @Override
    public List<ManagementProductListView> findProductListForManagement(int managerId) {
        int marketId = Optional.ofNullable(managementDAO.readMarketId(managerId)).orElseThrow(() -> {
            logger.error("Fatal error:::no mapping for manager to market");
            logger.error("Fatal error:::row, mapping manager id:{} to market id removed in tbl_managements.");
            throw new IllegalStateException("No mapping for manager to market.");
        });

        Map<LocalDate, List<Product>> productListGroupedByDate = 
                productDAO.readListByMarketId(marketId).stream()
                                                       .collect(Collectors.groupingBy(
                                                                   product -> product.getUploadTime().toLocalDate()));

        logger.debug("Finding product list succeeded.");

        List<ManagementProductListView> returnedList = new ArrayList<>();

        for (LocalDate date : productListGroupedByDate.keySet()) {
            ManagementProductListView mplv = new ManagementProductListView();

            mplv.setUploadDate(date);
            productListGroupedByDate.get(date).stream()
                                              .forEach(product -> 
                                                  mplv.addProductEntity(modelMapper.map(product, ProductListViewEntity.class)));
            returnedList.add(mplv);
        }

        returnedList.sort((pro1, pro2) -> {
            return (-1) * pro1.getUploadDate().compareTo(pro2.getUploadDate());
        });

        logger.debug("Returnning product list.");

        return returnedList;
    }

    /**
     * 지상 어플리케이션의 와이어프레임 상의 마켓 관리 화면에을 보면 상품 등록 일자 별로 그룹핑 된 상품 목록 화면과 해당 화면에서 특정 일자에
     * 대한 그룹을 선택할 경우 선택된 그룹의 상품 목록을 출력하는 화면이 존재한다. 이 메서드는 후자의 화면에 필요한 동작을 수행한다.
     */
    @Override
    public ManagementProductListView findProductListByDate(int managerId, LocalDate uploadDate) {
        int marketId = Optional.ofNullable(managementDAO.readMarketId(managerId)).orElseThrow(() -> {
            logger.error("Fatal error:::no mapping for manager to market");
            logger.error("Fatal error:::row, mapping manager id:{} to market id removed in tbl_managements.");
            throw new IllegalStateException("No mapping for manager to market.");
        });

        List<Product> productList = productDAO.readListByDate(marketId, uploadDate);

        logger.debug("Finding product list succeeded.");

        ManagementProductListView mplv = new ManagementProductListView();

        mplv.setUploadDate(uploadDate);
        productList.stream()
                   .forEach(product -> mplv.addProductEntity(modelMapper.map(product, ProductListViewEntity.class)));

        logger.debug("Returnning product list.");

        return mplv;
    }

    /**
     * 마켓 관리 화면 중 상품 수정 화면에 처음 접근할 때 호출될 메서드이다. 상품 정보를 수정하려면 상품의 기존 정보가 클라이언트에게
     * 전달되어야 한다. 상품 정보 수정 화면에서의 상품 정보 조회와 일반 유저의 쇼핑 화면에서의 상품 정보 조회는 반환되는 내용이 다를 수
     * 있으므로 서비스 계층의 상품 조회 로직 메서드를 두 개로 분리하였다.(상품 관리 화면/쇼핑 화면)
     */
    @Override
    public ProductModifyResponseDTO findProductForModifying(int managerId, int productId) {
        if (Objects.isNull(managementDAO.readMarketIdWithProductId(managerId, productId))) {
            logger.error("Fatal:::Illegal access detected with Account id : {}.", managerId);
            logger.error("Fatal:::Account id {} tried to access another market's product. product id:{}", managerId,
                    productId);

            throw new IllegalManagerAccessDetectedException("Illegal access to product management detected.", managerId,
                    productId);
        }

        Product product = productDAO.read(productId);

        logger.debug("Finding product info succeeded. Returnning {} type object.", ProductModifyResponseDTO.class);

        return modelMapper.map(product, ProductModifyResponseDTO.class);
    }

    // Methods related to product modification
    // ==========================================================================================================================

    /**
     * 상품 수정을 수행하는 메서드이다. 이 메서드와 이 메서드가 호출하는 다른 private 메서드들을 포함한 상품 수정 로직의 흐름은 다음과
     * 같다.우선 {@code productId}에 해당하는 마켓 id와 상품 수정을 요청한 유저({@code managerID})가 관리하는
     * 마켓 id를 비교한다. spring security의 access control을 이용해서 'ROLE_MANAGER' 유저만 해당 url에
     * 접근 가능 하게하여 일반 유저는 접근 못하도록 하긴 하였으나 다른 마켓의 관리자 계정으로 다른 마켓의 상품을 수정해버리는 일이 발생할 수
     * 있기 때문에 꼭 필요한 작업이다. (JWT token 내에 마켓 id 필드를 둘 수도 있겠으나 그렇게는 하지 않았다.) <br>
     *
     *
     * 그 다음으로 수행하는 작업은 상품 도메인을 구성하는 해시태그와 이미지 url(AWS S3 등의 스토리지에 저장된 이미지의 url)을
     * 삭제한다. 이들 정보는 현재 RDB의 'tbl_hashtags', 'tbl_images'에 저장되어 있다. 업데이트시에 기존 정보를 다
     * 삭제해버리는 것이 구현상 편한 것 같다.
     *
     * 마지막으로 S3와 같은 스토리지에 저장되어 있는 이미지 파일을 삭제한다. 실제 이미지 파일의 삭제를 이미지 url 삭제보다 나중에 할
     * 경우, 즉 이미지 url 삭제를 먼저 할 경우 이미지 url의 일부만 삭제된 상태에서 오류가 발생하거나 이미지 url은 모두 다
     * 삭제되었으나 실제 이미지 파일의 삭제 바로 이전에 오류가 발생할 경우 S3와 같은 스토리지에는 소유자({@link Product})의
     * 정체를 알 수 없는 쓰레기 파일들이 남아있게 된다고 생각할 수 있으나 {@link @Transactional}이 적용되었기 때문에 오류가
     * 발생하면 Rollback 되어 소유자의 정체를 알 수 없는 이미지 파일이 S3에 남게 될 걱정은 하지 않아도 된다.
     *
     * 또한 S3와 같은 스토리지에 저장된 이미지 파일을 먼저 삭제할 경우 일부의 이미지 파일만 삭제된 상태 혹은 이미지 파일은 모두 삭제
     * 되었으나 이미지 url 삭제 이전에 오류가 발생하면 존재하지도 않는 이미지 파일에 대한 url을 전달 받은 클라이언트는 (존재하지 않는
     * 이미지에 대한 요청이므로)쓸데 없이 네트워킹 낭비를 하게 될 것이다.
     *
     * 위와 같은 이유로 이미지 url을 먼저 삭제하기로 하였다.
     *
     */
    @Override
    @Transactional
    public void modifyProduct(int managerId, ProductModifyRequestDTO productDTO) {
        int productId = productDTO.getId();

        if (Objects.isNull(managementDAO.readMarketIdWithProductId(managerId, productId))) {
            logger.error("Fatal:::Illegal access detected with Account id : {}.", managerId);
            logger.error("Fatal:::Account id {} tried to access another market's product. product id:{}", managerId,
                    productId);

            throw new IllegalManagerAccessDetectedException("Illegal access to product management detected.", managerId,
                    productId);
        }

        Product olderProduct = productDAO.read(productId);

        List<String> olderImages = olderProduct.getImageUrls();
        String olderRepresentingImage = olderProduct.getRepresentingImageUrl();

        // 이미지 파일보다 먼저 삭제되어야만 함.
        deleteOlderImageUrls(productId);

        deleteOlderImages(olderRepresentingImage);
        deleteOlderImages(olderImages.toArray(new String[olderImages.size()]));

        deleteOlderHashTags(productId);

        modifyProductInternal(productDTO);
    }

    /**
     *
     * {@link #modifyProduct(int, ProductMutationDTO)} 메서드로부터 호출되는 메서드로 실제 상품 수정 작업을
     * 진행한다. 트랜잭션이 적용되지 않는 {@code multipartDAO}의 업로드 연산에 대한 예외 처리 및 상품 수정 작업 이전에 필요한
     * 기존 정보 삭제 작업 등으로 메서드의 코드 양이 너무 많아 분리하게 되었다.
     *
     * 현재 해시태그 정보가 RDB와 엘라스틱서치에 모두 저장되어 있어 이 메서드에서는 RDB와 엘라스틱서치 각각에 상품 정보를 저장한다.
     * 엘라스틱서치에 저장된 해시태그 정보는 검색어 미리보기 기능위해 이용된다. RDB에도 해시태그 정보가 중복으로 존재하는 이유는 다음과 같다.
     * 처음에는 해시태그를 통한 상품 검색 기능을 위해 {@link Product} 정보를 기존 RDB에서 엘라스틱서치로 이동할까를 고민해보았다.
     * 그러나 엘라스틱서치가 ROLLBACK과 같은 트랜잭션 지원을 제공하지 않는 것으로 알고 있어 {@link Product} 정보를 이동시키는
     * 것이 좋지 않을 수 있겠다는 생각이 들어 현재 일단 {@link Product} 정보가 RDB에 저장되어 있는 상황이다.
     *
     * 하나는 사용자가 입력(및 요청)한 해시태그 키워드를 이용해 엘라스틱서치로부터 관련 상품 id를 받아온 후 이 id를 이용해 RDB로부터
     * {@link Product} 정보를 얻어오는 것인데 이 방법은 두 종류의 데이터베이스(서버)에 접근해야만하기 때문에 전혀 좋은 방법이 아닌
     * 것 같다. 그래서 방법은 RDB에도 엘라스틱서치에 저장된 것과 동일한 해시태그 정보를 두어 (최소한 {@link Product} 정보가
     * RDB에 저장되어 있는 상황에서만큼은) 해시태그를 통한 상품 검색 기능은 엘라스틱서치를 전혀 이용하지 않고 바로 RDB를 검색하는
     * 방법이다.
     *
     * 위와 같은 이유로 해시태그 정보를 RDB와 엘라스틱서치에 동시에 두게 되었다.
     *
     */
    private void modifyProductInternal(ProductModifyRequestDTO productDTO) {

        List<String> newImages = null;
        String newRepresentingImage = null;

        try {
            newImages = multipartDAO
                            .upload(productDTO.getImages().toArray(new MultipartFile[productDTO.getImages().size()]));
            newRepresentingImage = multipartDAO.upload(productDTO.getRepresentingImage()).get(0);

            logger.debug("Uploading images to storage succeeded. Modifying product info started now.");

            Product updatingProduct = modelMapper.map(productDTO, Product.class);
            updatingProduct.setRepresentingImageUrl(newRepresentingImage);

            productDAO.update(updatingProduct);
            productDAO.createImages(newImages, updatingProduct.getId());
            productDAO.createHashTags(productDTO.getHashTags(), updatingProduct.getId());

            List<HashTag> hashtagList = new ArrayList<>();

            updatingProduct.getHashTags()
                           .stream()
                           .forEach(hashTag -> hashtagList.add(new HashTag(updatingProduct.getId(), hashTag)));

            hashTagDAO.saveAll(hashtagList);

        } catch (RuntimeException e) {
            logger.debug("Exception occurred while trying to modifying product info.");

            if (newImages != null) {
                logger.debug("Deleting just uploaded images from image storage.");

                deleteOlderImages(newImages.toArray(new String[newImages.size()]));
            }

            if (newRepresentingImage != null) {
                logger.debug("Deleting just uploaded representing image from image storage.");

                deleteOlderImages(newRepresentingImage);
            }

            throw e;
        }
    }

    // Methods related to product registering
    // ==========================================================================================================================

    /**
     *
     * 상품 등록 작업을 수행한다. 우선 스토리지에 상품 이미지를 업로드한 후 업로드 된 이미지의 url을 전달한 후
     * {@link #registerProductInternal(Product)} 메서드를 호출하여 RDB로의 상품 정보 등록 작업을 맡긴다.
     * {@code multipartDAO.upload()} 메서드 수행 중 일부 이미지가 업로드 된 상태에서 예외가 발생할 경우를 대비할 필요가
     * 있는데 이는 {@code multipartDAO.upload()} 메서드에서 처리한다.
     *
     */
    @Override
    @Transactional
    public void registerProduct(int managerId, ProductRegisterRequestDTO productRegisterDTO) {
        Objects.requireNonNull(productRegisterDTO,
                "Null value argument productRegisterDTO detected while trying to register product.");

        List<String> imageUrls = null;
        String representingImageUrl = null;

        int marketId = Optional.ofNullable(managementDAO.readMarketId(managerId)).orElseThrow(() -> {
            logger.error("Fatal error:::no mapping for manager to market");
            logger.error("Fatal error:::row, mapping manager id:{} to market id removed in tbl_managements.");
            throw new IllegalStateException("No mapping for manager to market.");
        });

        logger.debug("Finding market id succeeded.  market id : {}", marketId);

        try {
            imageUrls = multipartDAO.upload(
                    productRegisterDTO.getImages().toArray(new MultipartFile[productRegisterDTO.getImages().size()]));
            representingImageUrl = multipartDAO.upload(productRegisterDTO.getRepresentingImage()).get(0);

            logger.debug("Uploading product images using {} succeeded.", multipartDAO);

            Product product = modelMapper.map(productRegisterDTO, Product.class);
            product.setMarketId(marketId);
            product.setImageUrls(imageUrls);
            product.setRepresentingImageUrl(representingImageUrl);

            registerProductInternal(product);

        } catch (RuntimeException e) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "Starting to check whether images have been uploaded or not to cloud storage due to occurrence of {}",
                        e.toString());
            }

            if (imageUrls != null) {
                logger.info("Deleting images from cloud storage. Images : {}", imageUrls);

                deleteOlderImages(imageUrls.toArray(new String[imageUrls.size()]));
            }

            if (representingImageUrl != null) {
                logger.info("Deleting representing image from cloud storage. representing image: {}",
                        representingImageUrl);

                deleteOlderImages(representingImageUrl);
            }

            throw e;
        }
    }

    /**
     * {@link #registerProduct(int, ProductMutationDTO)} 메서드로부터 호출되는 private 메서드로
     * 실질적인 데이터 등록 작업을 수행한다. 이 메서드를 정의한 특별한 이유는 따로 없으며 스토리지 관련 예외 처리 등으로 코드가 매우 많은
     * {@link #registerProduct(int, ProductMutationDTO)} 메서드의 코드를 조금 분산시키기 위함이다.
     *
     * hashtag dao에 해시태그를 또 저장하느 ㄴ이유
     */
    private void registerProductInternal(Product product) {

        productDAO.create(product);

        int productId = product.getId();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "Creating product data with id : {} succeeded. Processing product images & hashtags info creation....",
                    productId);
        }

        productDAO.createImages(product.getImageUrls(), productId);
        productDAO.createHashTags(product.getHashTags(), productId);

        List<HashTag> hashTags = new ArrayList<>();
        product.getHashTags()
               .stream()
               .forEach(hashTag -> {
                   HashTag hashTagEntity = new HashTag();
                   hashTagEntity.setContent(hashTag);
                   hashTagEntity.setProductId(productId);

                   hashTags.add(hashTagEntity);
               });

        try {
            hashTagDAO.saveAll(hashTags);
        } catch (ElasticsearchException e) {
            if (logger.isInfoEnabled()) {
                logger.info("{} occurred while trying to save hashtags to elasticsearch server with product id : {}",
                        e.toString(), productId);
            }

            try {
                hashTagDAO.deleteAllByProductId(productId);
            } catch (Exception ex) {
                logger.error("Despite of retryed deletion, Deleting hashtags from storage failed.");
                logger.error("Manual deletion required. Failed hashtag's product id : {}", productId);
                e.addSuppressed(ex);
            }

            throw e;
        }
    }
}
