package com.jisang.dto.product;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

import com.jisang.support.validation.ImageExtension;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 지상 어플리케이션 와이어프레임상의 마켓 관리 화면들에 사용될 DTO 클래스들이 내부적으로 정의되어있으며 이들 정적 내부 클래스에 공통으로
 * 사용될 프로퍼티들이 정의되어 있다. 처음에는 상품 등록 및 수정 과정에 필요한 모든 요청/응답에 하나의 DTO 클래스를 이용하려고 하였다.
 * 그러나 springfox에 {@link @JsonView} 또는 {@link ApiModelProperty}의
 * {@code readOnly} 와 {@code accessMode.READ_ONLY} 어트리뷰트가 제대로 동작되지 않아 DTO 클래스를
 * 상황에 맞게 나누게 되었다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class ProductManagementViewDTO {

    @ApiModelProperty(notes = "상품 카테고리 코드. ", name = "category", required = true, allowableValues = "range[30, 38]", example = "OUTER : '31", value = "코드 북에 지정된 카테고리 코드를 사용해야 한다.")
    @NotBlank(message = "상품 카테고리 정보를 입력해 주세요.")
    @Pattern(regexp = "^3[0-8]$", message = "상품 카테고리 정보가 올바르지 않습니다.")
    private String category;

    @ApiModelProperty(notes = "상품 이름. ", name = "name", required = true)
    @NotBlank(message = "상품 이름을 입력해 주세요.")
    private String name;

    @ApiModelProperty(notes = "상품 가격.", name = "price", required = true)
    @NotBlank(message = "상품 가격을 입력해 주세요.")
    private String price;

    @ApiModelProperty(notes = "상품 설명.", name = "detail", required = true)
    @NotBlank(message = "상품 소개글을 입력해 주세요.")
    private String detail;

    @ApiModelProperty(notes = "상품 관련 해시태그.", name = "hashTags", required = true)
    @NotEmpty(message = "해시 태그를 입력해 주세요.")
    private List<@NotBlank(message = "해시태그 정보가 올바르지 않습니다.") String> hashTags;

    @ApiModelProperty(notes = "해당 상품을 마켓 추천 상품으로 할지 말지를 결정.", name = "recommended", required = true)
    private boolean recommended;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(List<String> hashTags) {
        this.hashTags = hashTags;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    @Override
    public String toString() {
        return getClass() + getName() + "[category=" + category + ", name=" + name + ", price=" + price + ", detail="
                + detail + ", hashTags=" + hashTags + ", recommended=" + recommended;
    }

    /**
     * 
     * 상품 정보에 대한 등록 또는 변경 요청에 공통으로 필요한 구성 정보를 담은 DTO 클래스이다.
     * 
     * 
     * @author leeseunghyun
     *
     */
    public abstract static class ProductMutationDTO extends ProductManagementViewDTO {

        @ApiModelProperty(notes = "상품 대표 이미지.", name = "representingImage", required = true, value = "jpg | png | tif 등의 올바른 이미지 파일 포맷이어야 한다. 해당 프로퍼티는 상품 수정 화면에 대한 응답시에는 포함되지 않는다.")
        @ImageExtension(message = "올바른 이미지 파일 형식이 아닙니다.")
        private MultipartFile representingImage;

        @ApiModelProperty(notes = "상품 이미지.", name = "images", required = true, value = "jpg | png | tif 등의 올바른 이미지 파일 포맷이어야 한다. 해당 프로퍼티는 상품 수정 화면에 대한 응답시에는 포함되지 않는다.")
        @NotEmpty(message = "최소한 1개 이상의 상품 이미지를 등록해 주세요.")
        private List<@ImageExtension(message = "올바른 이미지 파일 형식이 아닙니다.") MultipartFile> images;

        public MultipartFile getRepresentingImage() {
            return representingImage;
        }

        public void setRepresentingImage(MultipartFile representingImage) {
            this.representingImage = representingImage;
        }

        public List<MultipartFile> getImages() {
            return images;
        }

        public void setImages(List<MultipartFile> images) {
            this.images = images;
        }

        @Override
        public String toString() {
            return super.toString() + ", representingImage=" + representingImage + ", images=" + images + "]";
        }

    }

    /**
     * 
     * 상품 정보 수정 요청에 필요한 구성 정보를 담은 DTO 클래스이다. 현재 구현으로는 상품 수정과 등록에 다른 구성 정보가 존재하지는 않지만
     * 후에 이런 부분은 충분히 추가될 수 있다고 생각한다. 에를들어, EAN-13(국제 상품 번호)은 상품 등록에는 필요한 정보일 수 있으나
     * 수정에는 적절치 않은 정보이다.
     * 
     * 
     * @author leeseunghyun
     *
     */
    public static class ProductModifyRequestDTO extends ProductMutationDTO {

        /**
         * 경로 변수로부터 취득한 값을 컨틀로러의 메서드에서 set 해줄 프로퍼티로 API doc에는 표시될 필요가 없다.
         */
        @ApiModelProperty(hidden = true)
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return super.toString() + ", id =" + id + "]";
        }
    }

    /**
     * 
     * 상품 정보 등록 요청에 필요한 구성 정보를 담은 DTO 클래스이다. 현재 구현으로는 상품 수정과 등록에 다른 구성 정보가 존재하지는 않지만
     * 후에 이런 부분은 충분히 추가될 수 있다고 생각한다. 에를들어, EAN-13(국제 상품 번호)은 상품 등록에는 필요한 정보일 수 있으나
     * 수정에는 적절치 않은 정보이다.
     * 
     */
    public static class ProductRegisterRequestDTO extends ProductMutationDTO {
    }

    /**
     * 
     * 상품 정보 수정을 위한 기존 상품 정보 요청에 대한 응답과 상품 정보가 수정되면 수정된 상품 정보를 화면에 표시하기위한 응답에 사용된다.
     * 
     * 
     * @author leeseunghyun
     *
     */
    public static class ProductModifyResponseDTO extends ProductManagementViewDTO {

        @ApiModelProperty(notes = "상품 대표 이미지의 url.", name = "representingImageUrl", value = "해당 프로퍼티는 상품 수정 화면에 대한 응답에(만) 포함되는 정보이다.")
        private String representingImageUrl;

        @ApiModelProperty(notes = "상품 이미지들의 url.", name = "imageUrl", value = "해당 프로퍼티는 상품 수정 화면에 대한 응답에(만) 포함되는 정보이다.")
        private List<String> imageUrls;

        public String getRepresentingImageUrl() {
            return representingImageUrl;
        }

        public void setRepresentingImageUrl(String representingImageUrl) {
            this.representingImageUrl = representingImageUrl;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @Override
        public String toString() {
            return super.toString() + ", representingImageUrl=" + representingImageUrl 
                                    + ", imageUrls=" + imageUrls + "]";
        }
    }

}
