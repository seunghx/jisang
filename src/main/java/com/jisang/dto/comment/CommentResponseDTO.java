package com.jisang.dto.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 현재의 지상 어플리케이션 와이어 프레임의 상품 상세 정보 보기 화면에는 두 타입 프레그먼트가 존재한다. 하나는 상품 이미지 외의 모든 상품
 * 정보를 보여주는 화면(상품 상세 정보)이며 다른 프레그먼트는 해당 상품에 대한 문의 댓글 화면이다. 특정 상품 id를 url 경로 변수로
 * 받아 상품 정보를 반환하는 과정에서 해당 상품에 대한 댓글을 응답에 함께 전달하여도 되나 프레그먼트가 나눠진 만큼 일단은 상품 상세와 댓글
 * 정보에 대한 요청 및 응답을 분리하기로 하였다. (클라이언트에서는 상품 상세 화면에서 댓글 화면 프레그먼트로 이동할 때 서버에 댓글 정보에
 * 대한 요청을 보내야한다.) 이렇게 하지 않고 상품 정보 요청시 댓글까지 함께 전달하게 하면 후에 화면이 변경되어도 수정의 범위가 좁을
 * 것이다. 그러나 현재와 같은 화면의 경우 어플리케이션 사용자가 댓글 프레그먼트를 전혀 보지 않을 경우 응답 데이터만 낭비되는 꼴이 된다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class CommentResponseDTO {

    @ApiModelProperty(notes = "댓글 id.", name = "commentId")
    private int commentId;
    @ApiModelProperty(notes = "댓글 내용.", name = "content")
    private String content;
    @ApiModelProperty(notes = "댓글 작성자 이름.", name = "userName")
    private String userName;
    @ApiModelProperty(notes = "댓글 등록 시간.", name = "uploadTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime uploadTime;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(notes = "대댓글", name = "childs", readOnly = true)
    private List<CommentResponseDTO> childs = new ArrayList<>();

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public List<CommentResponseDTO> getChilds() {
        return childs;
    }

    public void setChilds(List<CommentResponseDTO> childs) {
        this.childs = childs;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[commentId=" + commentId + ", content=" + content + ", userName=" + userName
                + ", uploadTime=" + uploadTime + "]";
    }

}
