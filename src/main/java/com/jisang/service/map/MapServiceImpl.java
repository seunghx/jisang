package com.jisang.service.map;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jisang.domain.Location;
import com.jisang.domain.Market;
import com.jisang.dto.MallMapResponseDTO;
import com.jisang.dto.MallMapResponseDTO.MapAddressResponseDTO;
import com.jisang.persistence.MapDAO;
import com.jisang.persistence.MarketDAO;
import com.jisang.support.NoSuchLocationException;
import com.jisang.support.NoSuchMarketException;

@Service
public class MapServiceImpl implements MapService{

	private final Logger logger = LoggerFactory.getLogger(MapServiceImpl.class);
	
	@Autowired
	private MapDAO mapDAO;
	@Autowired
	private MarketDAO marketDAO;
	@Autowired
	private ModelMapper modelMapper;
	
	
	@Override
	public MallMapResponseDTO getMallMapByLocationId(String locationId) {
		
		Location location = Optional.ofNullable(mapDAO.readLocation(locationId))
									.orElseThrow(() -> {
										logger.info("Location info doesn't exist. Invalid location id : {}", locationId);
										
										throw new NoSuchLocationException("Received invalid location id parameter.", locationId);
									});
		
		return modelMapper.map(location, MallMapResponseDTO.class);
	}
	

	@Override
	public MallMapResponseDTO getMallMapWithMarketMarker(int marketId) {
		Market market = Optional.ofNullable(marketDAO.read(marketId))
								.orElseThrow(() -> {
									logger.info("Received market id doesn't exist on Database. Invalid market id : {}.", marketId);
									
									throw new NoSuchMarketException("Received non-existing market id while trying to get map info.", marketId);
								});
		
		logger.debug("Get market info succeeded while trying to get map location info for market id {}.", marketId);
		
		MallMapResponseDTO mapDTO = 
					modelMapper.map(mapDAO.readLocation(market.getLocation()), MallMapResponseDTO.class);
		
		
		MapAddressResponseDTO dto = mapDTO.getAddressList()
										  .stream()
							   			  .filter(addr -> addr.getMarket() != null && addr.getMarket().getId() == marketId)
							   			  .findAny()
							   			  .orElseThrow(() -> {
							   				  logger.warn("No mapping information for market with id : {} and address.", marketId);
							   				  logger.warn("A row or column in database Address table might be deleted.");
							   				  
							   				  throw new IllegalStateException("No mapping for market with address.");
							   			  });
		dto.setMarked(true);
				
		return mapDTO;
	}

}
