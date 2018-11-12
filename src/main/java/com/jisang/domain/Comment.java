package com.jisang.domain;

import java.time.LocalDateTime;

/**
 * 
 * 상품 댓글에 대한 도메인 클래스.
 * 
 * @author leeseunghyun
 *
 */
public class Comment {

    private int id;
    private int userId;
    private int productId;

    /**
     * 이 클래스의 {@code parentId} 프로퍼티는 대댓글(댓글에 대한 댓글)을 위하여 정의되었다. 현재 구현은 깊이2(댓글)까지만
     * 가능하며 이 부분(depth)에 대한 처리는 서비스 오브젝트 등에서 댓글 업로드시 검사한다.
     */
    private int parentId;
    private String content;
    private LocalDateTime uploadTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + ", userId=" + userId + ", productId=" + productId + ", parentId="
                + parentId + ", content=" + content + ", uploadTime=" + uploadTime + "]";
    }

}
