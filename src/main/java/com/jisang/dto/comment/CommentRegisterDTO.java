package com.jisang.dto.comment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 댓글 작성 처리 과정에 사용될 DTO 클래스이다.
 * 
 * @author leeseunghyun
 *
 */
public class CommentRegisterDTO {

    /** JWT token으로부터 작성자 id를 가져오기 때문에 이 부분은 API 상에서 보이지 않도록 hidden으로 하였다. */
    @ApiModelProperty(hidden = true)
    private int userId;

    @Positive
    @ApiModelProperty(notes = "작성하려는 댓글의 상품 id", name = "productId", required = true)
    private int productId;

    @PositiveOrZero
    @ApiModelProperty(notes = "대댓글 작성시 부모 댓글의 id. 부모 댓글 작성의 경우 0을 전달해야 한다.", name = "parentId", required = true)
    private int parentId;
    @NotBlank
    @ApiModelProperty(notes = "댓글 내용.", name = "content", required = true, value = "옷이 이쁘네요!")
    private String content;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[userId=" + userId + "productId=" + productId + ", parentId=" + parentId
                                    + ", content=" + content + "]";
    }

}
