package com.jisang.dto.market;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jisang.support.validation.ExistingMallLocation;
import com.jisang.support.validation.ImageExtension;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 지상 어플리케이션 와이어 프레임 상의 마켓 정보 수정 화면에 사용 될 DTO 클래스이다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class MarketManagementDTO {

    @NotBlank(message = "마켓 이름을 입력해주세요.")
    @ApiModelProperty(notes = "마켓 이름.", name = "name", required = true, example = "지상 마켓")
    private String name;

    @ApiModelProperty(notes = "지하상가 내 마켓 위치 정보.", name = "address", required = true, example = "A-7")
    @NotBlank(message = "지하상가 내 마켓 위치 정보를 입력해주세요. 예) 'A-7'.")
    private String address;

    @ApiModelProperty(notes = "지하상가 위치 코드.", name = "location", required = true, example = "강남 : '11'", value = "코드북에 지정된 위치 코드를 사용해야 한다.")
    @NotBlank(message = "지하상가 위치 코드를 입력하세요. 예) 강남 : '11'.")
    @ExistingMallLocation(message = "올바른 지하상가 정보를 입력하세요.")
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[name=" + name + ", address=" + address + ", location=" + location;
    }

    public static class MarketModifyRequestDTO extends MarketManagementDTO {

        @ApiModelProperty(hidden = true)
        private int id;

        @ApiModelProperty(hidden = true)
        private int managerId;

        @ApiModelProperty(notes = "마켓 대표 이미지.", name = "imageFile", value = "이미지 파일. jpg | png | tif 등의 올바른 이미지 파일 포맷을 사용해야 하며 이 프로퍼티는 응답에서는 제외 된다.")
        @ImageExtension(message = "올바른 이미지 파일 형식이 아닙니다.")
        private MultipartFile imageFile;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getManagerId() {
            return managerId;
        }

        public void setManagerId(int managerId) {
            this.managerId = managerId;
        }

        public MultipartFile getImageFile() {
            return imageFile;
        }

        public void setImageFile(MultipartFile imageFile) {
            this.imageFile = imageFile;
        }

        @Override
        public String toString() {
            return super.toString() + ", id=" + id + ", managerId=" + managerId + ", imageFile=" + imageFile + "]";
        }

    }

    public static class MarketModifyResponseDTO extends MarketManagementDTO {

        /**
         * S3 등 스토리지 서비스에 저장된 파일의 이름을 나타내는 문자열 프로퍼티이다. RDB에는 {@code imageFile}이 아닌 이
         * 프로퍼티 {@code image}가 저장된다.
         */
        @ApiModelProperty(notes = "마켓 대표 이미지 url.", name = "image")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public String toString() {
            return super.toString() + ", imageUrl=" + imageUrl + "]";
        }
    }
}
