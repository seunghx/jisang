package com.jisang.service.product;

import com.jisang.dto.product.ProductListViewDTO.ManagementProductListView;
import com.jisang.dto.product.ProductListViewDTO.ProductListView;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyRequestDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductModifyResponseDTO;
import com.jisang.dto.product.ProductManagementViewDTO.ProductRegisterRequestDTO;

import java.time.LocalDate;
import java.util.List;

import com.jisang.dto.product.ProductListViewConfigData;
import com.jisang.dto.product.ProductShoppingDetailedViewDTO;

/**
 * 
 * {@link Product} 도메인 관련 비즈니스 로직을 정의한 서비스 인터페이스이다.
 *
 *
 * @author leeseunghyun
 *
 */
public interface ProductService {

    public ProductShoppingDetailedViewDTO findProductForShopping(int productId);

    public ProductListView findProductList(ProductListViewConfigData viewConfig);

    // For product(market) management
    // ==========================================================================================================================

    public void registerProduct(int managerId, ProductRegisterRequestDTO dto);

    public void modifyProduct(int managerId, ProductModifyRequestDTO productDTO);

    public ProductModifyResponseDTO findProductForModifying(int managerId, int productId);

    public List<ManagementProductListView> findProductListForManagement(int managerId);

    public ManagementProductListView findProductListByDate(int managerId, LocalDate uploadDate);

    public void deleteProduct(int managerId, int productId);
}
