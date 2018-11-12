package com.jisang.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jisang.domain.Address;
import com.jisang.domain.Comment;
import com.jisang.domain.Location;
import com.jisang.domain.Market;
import com.jisang.dto.MallMapResponseDTO;
import com.jisang.dto.MallMapResponseDTO.MapAddressResponseDTO;
import com.jisang.dto.comment.CommentRegisterDTO;
import com.jisang.dto.market.MarketManagementDTO.MarketModifyResponseDTO;
import com.jisang.dto.user.SignupManagerDTO;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.createTypeMap(SignupManagerDTO.class, Market.class).addMapping(SignupManagerDTO::getMarketName,
                Market::setName);

        modelMapper.createTypeMap(Address.class, MarketModifyResponseDTO.class).addMapping(Address::getId,
                MarketModifyResponseDTO::setAddress);

        modelMapper.addConverter(commentConverter());
        // modelMapper.addConverter(locationToMapDTOConverter());

        return modelMapper;
    }

    /*
     * private Converter<Location, MallMapResponseDTO> locationToMapDTOConverter() {
     * return new AbstractConverter<Location, MallMapResponseDTO>() { private final
     * ModelMapper modelMapper = new ModelMapper();
     * 
     * @Override protected MallMapResponseDTO convert(Location location) {
     * MallMapResponseDTO mapDTO = new MallMapResponseDTO();
     * 
     * Objects.requireNonNull(location, "Received null value source object.");
     * 
     * List<Address> addresses = location.getAddressList();
     * 
     * if(Objects.isNull(addresses) || addresses.isEmpty()) { throw new
     * IllegalArgumentException("Source object with invalid property."); }
     * 
     * List<MapAddressResponseDTO> addressDTOList = new ArrayList<>();
     * 
     * addresses.stream().forEach(addr -> { addressDTOList.add(modelMapper.map(addr,
     * MapAddressResponseDTO.class)); });
     * 
     * mapDTO.setAddressList(addressDTOList);
     * 
     * // Set degree mapDTO.setTopLeftLATDegree((int)location.getTopLeftLAT());
     * mapDTO.setTopLeftLNGDegree((int)location.getTopLeftLNG());
     * mapDTO.setBottomRightLATDegree((int)location.getBottomRightLAT());
     * mapDTO.setBottomRightLNGDegree((int)location.getBottomRightLNG());
     * 
     * // Degree to seconds double topLeftLATSec = (location.getTopLeftLAT() -
     * (int)location.getTopLeftLAT()) * 3600; double topLeftLNGSec =
     * (location.getTopLeftLNG() - (int)location.getTopLeftLNG()) * 3600; double
     * bottomRightLATSec = (location.getBottomRightLAT() -
     * (int)location.getBottomRightLAT()) * 3600; double bottomRightLNGSec =
     * (location.getBottomRightLNG() - (int)location.getBottomRightLNG()) * 3600;
     * 
     * // Set seconds
     * mapDTO.setTopLeftLATSec(Double.parseDouble(String.format("%.2f",
     * topLeftLATSec)));
     * mapDTO.setTopLeftLNGSec(Double.parseDouble(String.format("%.2f",
     * topLeftLNGSec)));
     * mapDTO.setBottomRightLATSec(Double.parseDouble(String.format("%.2f",
     * bottomRightLATSec)));
     * mapDTO.setBottomRightLNGSec(Double.parseDouble(String.format("%.2f",
     * bottomRightLNGSec)));
     * 
     * return mapDTO; } }; }
     */

    private Converter<CommentRegisterDTO, Comment> commentConverter() {
        return new AbstractConverter<CommentRegisterDTO, Comment>() {

            @Override
            protected Comment convert(CommentRegisterDTO source) {
                Objects.requireNonNull(source, "Received null value source.");

                Comment comment = new Comment();
                comment.setUserId(source.getUserId());
                comment.setParentId(source.getParentId());
                comment.setProductId(source.getProductId());
                comment.setContent(source.getContent());

                return comment;
            }
        };
    }
}
