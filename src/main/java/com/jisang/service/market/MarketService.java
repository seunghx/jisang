package com.jisang.service.market;

import com.jisang.dto.market.MarketManagementDTO.MarketModifyRequestDTO;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyResponseDTO;

/**
 * 
 * {@link Market} 도메인 관련 비즈니스 로직을 정의한 서비스 인터페이스이다.
 *
 * @author leeseunghyun
 *
 */
public interface MarketService {
    // 현재 지상 어플리케이션(와이어 프레임 상)에 존재하지 않는 기능.
    // public MarketResponseDTO findMarket(int marketId);

    public MarketModifyResponseDTO findMarketForManagement(int managerId);

    public MarketModifyResponseDTO modifyMarket(MarketModifyRequestDTO marketDTO);
}
