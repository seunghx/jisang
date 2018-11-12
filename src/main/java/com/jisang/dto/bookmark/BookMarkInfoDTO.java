package com.jisang.dto.bookmark;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 북마크 정보를 담은 DTO 클래스이다.
 * 
 * @author leeseunghyun
 *
 */
public class BookMarkInfoDTO {

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private int userId;

    @ApiModelProperty(notes = "마켓 id.", name = "marketId")
    private int marketId;

    // properties for response

    @ApiModelProperty(notes = "마켓 이름.", name = "marketName", example = "지상 마켓")
    private String marketName;
    @ApiModelProperty(notes = "마켓 위치 코드.", name = "marketLocation", example = "강남 : '11'.")
    private String marketLocation;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketLocation() {
        return marketLocation;
    }

    public void setMarketLocation(String marketLocation) {
        this.marketLocation = marketLocation;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[userId=" + userId + ", marketId=" + marketId + ", marketName=" + marketName
                                    + ", marketLocation=" + marketLocation + "]";
    }

}
