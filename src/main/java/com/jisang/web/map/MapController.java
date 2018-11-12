package com.jisang.web.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

import com.jisang.config.code.CodeBook.MallLocation;
import com.jisang.dto.ErrorDTO;
import com.jisang.dto.MallMapResponseDTO;
import com.jisang.service.map.MapService;
import com.jisang.support.AddressAleadyUsedException;
import com.jisang.support.NoSuchLocationException;


@Controller
public class MapController {
	
	private final Logger logger = LoggerFactory.getLogger(MapController.class);
	
	@Autowired
	private MapService mapService;
	@Autowired
	private MessageSource msgSource;
	
	
	/**
	 * 마켓 등록(매니저 회원가입) 또는 마켓 수정에 대한 요청 파라미터로 이미 다른 마켓에 의해 사용중인 점포 id값이 전달될 수 있다. 
	 * 이 경우 {@link AddressAleadyUsedException}이 발생한다.
	 */
	@ExceptionHandler(NoSuchLocationException.class)
	public ResponseEntity<ErrorDTO> onNoSuchLocation(NoSuchLocationException ex, WebRequest request){
		logger.error("An exception occurred associated with non-existing location code", ex);
		
		StringBuilder builder = new StringBuilder();
		
		for(MallLocation loc : MallLocation.values()) {
			builder.append(", " + loc.getCode());
		}
		builder.substring(2);
		
		ErrorDTO errDTO = new ErrorDTO(HttpStatus.BAD_REQUEST.value()
								, msgSource.getMessage("response.exception.NoSuchLocationException"
													 , new String[] {ex.getLocationId(), builder.toString()}, request.getLocale()));
		
		return new ResponseEntity<>(errDTO, HttpStatus.BAD_REQUEST);
	}
	
	
	@GetMapping("/map/market/{marketId}")
	public String getMallMapWithMarketMarker(@PathVariable("marketId") int marketId, Model model){
		
		MallMapResponseDTO mapDTO = mapService.getMallMapWithMarketMarker(marketId);
		model.addAttribute("mapInfo", mapDTO);
		
		return "jisangMap";
	}
	
	
	@GetMapping("/map/location/{mallLocation}")
	public String getMallMap(@PathVariable("mallLocation") String mallLocation, Model model) {
		
		MallMapResponseDTO mapDTO = mapService.getMallMapByLocationId(mallLocation);
		model.addAttribute("mapInfo", mapDTO);

		return "jisangMap";
	}
	
}
