package com.jisang.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

/**
 * 
 * elasticsearch의 document로 저장 될 해시태그 도메인 클래스.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Document(indexName = "product", type = "hashtag", createIndex = false)
public class HashTag {

    @Id
    @JsonIgnore
    private String id;
    @Field(type = Text)
    private String content;
    private int productId;

    public HashTag() {
    }

    public HashTag(int productId, String content) {
        this.productId = productId;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + ", content=" + content + ", productId=" + productId + "]";
    }
}
