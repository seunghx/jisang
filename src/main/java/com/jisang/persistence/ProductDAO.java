package com.jisang.persistence;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jisang.domain.Product;
import com.jisang.dto.product.ProductListViewConfigData;
import com.jisang.dto.product.criteria.BestViewCriteria;
import com.jisang.dto.product.criteria.PageViewCriteria;
import com.jisang.dto.product.criteria.RecommendedViewCriteria;

/**
 * 지상 어플리케이션에서 판매되는 상품 도메인에 대한 DAO 인터페이스.
 * 
 * @author leeseunghyun
 */
public interface ProductDAO extends MybatisMapper {

    public void create(Product product);

    public Product read(int productId);

    public int readProductTotalCount(ProductListViewConfigData listViewConfig);

    public List<Product> readListBest(BestViewCriteria bestViewCriteria);

    public List<Product> readListPage(PageViewCriteria pageViewCriteria);

    public List<Product> readListRecommended(RecommendedViewCriteria recViewCriteria);

    public List<Product> readListByDate(@Param("marketId") int marketId, @Param("uploadDate") LocalDate uploadDate);

    public List<Product> readListByMarketId(int marketId);

    public List<Product> readByHashTag(String keyword);

    public void update(Product product);

    public void addHit(int productId);

    public void refreshHit();

    /**
     * 해시태그와 상품 이미지는 PRODUCT 도메인을 구성하는 정보이다. 하나의 PRODUCT에 대하여 여러 개의 해시태그와 이미지가 존재하므로
     * 이들 정보에 대한 테이블을 따로 빼두었다. 아직까지는 해시태그와 이미지(이미지가 저장된 storage상의 url)가 PRODUCT를
     * 구성하는 데이터 이상의 의미를 갖지는 않기 때문에 따로 도메인으로 취급하지는 않았으며 그러므로 PRODUCT 도메인에 한 data
     * access를 수행 로직이 담긴 이 DAO 인터페이스에 해시태그와 이미지(url)에 대한 입력 메서드를 정의하였다.
     */
    public void createImages(@Param("images") List<String> images, @Param("productId") int productId);

    public void createHashTags(@Param("hashTags") List<String> hashTags, @Param("productId") int productId);

    public void delete(int productId);

    public void deleteImages(int productId);

    public void deleteHashTags(int productId);

}
