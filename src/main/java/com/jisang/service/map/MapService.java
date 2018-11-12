package com.jisang.service.map;

import com.jisang.dto.MallMapResponseDTO;

public interface MapService {
	
	public MallMapResponseDTO getMallMapByLocationId(String locationId);
	public MallMapResponseDTO getMallMapWithMarketMarker(int marketId);
		
}
