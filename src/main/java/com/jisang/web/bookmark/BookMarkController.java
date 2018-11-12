package com.jisang.web.bookmark;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.jisang.dto.bookmark.BookMarkInfoDTO;
import com.jisang.service.bookmark.BookMarkService;
import com.jisang.support.UserID;
import com.jisang.support.UserIDArgumentResolver;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@Api(description = "Bookmark related API")
public class BookMarkController {

    private Logger logger = LoggerFactory.getLogger(BookMarkController.class);

    @Autowired
    private BookMarkService bookMarkService;

    /**
     * 방문한 마켓에 대해 즐겨찾기가 설정 되었는지에 대한 정보를 반환한다. 유저가 해당 마켓에 대하여 기존에 즐겨찾기를 설정하였다면
     * HttpStatus.OK 상태 코드가 반환될 것이며 그렇지 않다면 즐겨찾기 설정을 하지 않았다는 의미로
     * {@link HttpStatus.NOT_FOUND}가 반환된다.
     * 
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해 null 검사가
     * 수행되기 때문에 안전하다고 판단하고 여기선 따로 같은 동작을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터
     * {@code userId}가 전달 된다고 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로
     * 잘못된 정보가 계속 전달되지는 않을 것이다.
     * 
     *
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @ApiOperation(value = "특정 마켓에 대한 북마크 정보")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "marketId", value = "방문한 마켓 id.", dataType = "int", paramType = "path", required = true),
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header") })
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not Found") })
    @GetMapping("/auth/bookmark/{marketId}")
    public ResponseEntity<Void> getBookMark(@PathVariable("marketId") int marketId, UserID userId) {

        BookMarkInfoDTO bookMarkInfo = new BookMarkInfoDTO();
        bookMarkInfo.setUserId(userId.getUserId());
        bookMarkInfo.setMarketId(marketId);

        logger.debug("Calling {} to get bookmark info for market id : {} started.", bookMarkService, marketId);

        if (bookMarkService.isBookMarked(bookMarkInfo)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 
     * 유저의 즐겨찾기 목록을 반환한다.
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해
     * {@link Authentication} 타입 오브젝트에 대한 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로
     * 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터 {@code userId}가 전달 된다고
     * 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 않을
     * 것이다.
     * 
     * 
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @GetMapping("/auth/bookmarks")
    @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 401, message = "Unauthorized") })
    public ResponseEntity<List<BookMarkInfoDTO>> getBookMarks(UserID userId) {

        if (logger.isDebugEnabled()) {
            logger.debug("Calling {} to get bookmark info for user id : {} started.", bookMarkService,
                    userId.getUserId());
        }

        List<BookMarkInfoDTO> bookmarkList = bookMarkService.findBookmarks(userId.getUserId());

        if (logger.isDebugEnabled()) {
            logger.debug("Get bookmark list for user id : {} succeeded.", userId.getUserId());
        }

        return new ResponseEntity<>(bookmarkList, HttpStatus.OK);

    }

    /**
     * 
     * 토글 방식으로 방문한 마켓에 대한 즐겨찾기 정보를 변경한다.
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해
     * {@link Authentication} 타입 오브젝트에 대한 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로
     * 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터 {@code userId}가 전달 된다고
     * 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 않을
     * 것이다.
     * 
     * 
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "marketId", value = "방문한 마켓 id.", dataType = "int", paramType = "path", required = true),
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header") })
    @ApiResponses({ @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 401, message = "Unauthorized") })
    @PutMapping("/auth/bookmark/{marketId}")
    public ResponseEntity<Void> putBookMark(@PathVariable("marketId") int marketId, UserID userId) {

        BookMarkInfoDTO bookMarkInfo = new BookMarkInfoDTO();
        bookMarkInfo.setUserId(userId.getUserId());
        bookMarkInfo.setMarketId(marketId);

        logger.debug("Calling {} to update bookmark info for market id : {} started.", bookMarkService, marketId);

        bookMarkService.modifyBookMark(bookMarkInfo);

        logger.debug("bookmark info update for market id : {} succeeded.", marketId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 해당 마켓에 대한 즐겨찾기 설정을 해제한다. 지상 어플리케이션의 와이어 프레임상의 즐겨찾기 목록 화면에 있는 x 버튼을 누를 경우 이
     * 핸들러 메서드가 호출된다.
     * 
     * 파라미터 {@code userId}는 seucirty 단과 {@link UserIDArgumentResolver}를 통해
     * {@link Authentication} 타입 오브젝트에 대한 유효성 검사가 수행된 후 전달되기 때문에 안전하다고 판단하고 여기선 따로
     * 같은 동작(null 검사 등)을 반복하지는 않는다. 만약 무언가 잘못되어 null 값의 파라미터 {@code userId}가 전달 된다고
     * 하여도 메서드의 시작부에서 {@link NullPointerException}이 발생할 것이므로 잘못된 정보가 계속 전달되지는 않을
     * 것이다.
     * 
     * 
     * @param userId
     *            - {@link UserID}, {@link UserIDArgumentResolver}
     * 
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "marketId", value = "선택한 마켓 id.", dataType = "int", paramType = "path", required = true),
            @ApiImplicitParam(name = "Authorization", value = "JWT 토큰이 담길 헤더. 접두어로 'Bearer : '가 붙는 형식.", required = true, dataType = "string", paramType = "header") })
    @ApiResponses({ @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 401, message = "Unauthorized") })
    @DeleteMapping("/auth/bookmark/{marketId}")
    public ResponseEntity<Void> deleteBookMark(@PathVariable("marketId") int marketId, UserID userId) {

        BookMarkInfoDTO bookMarkInfo = new BookMarkInfoDTO();
        bookMarkInfo.setUserId(userId.getUserId());
        bookMarkInfo.setMarketId(marketId);

        if (logger.isDebugEnabled()) {
            logger.debug("Calling {} to delete bookmark info for user id : {} started.", bookMarkService,
                    userId.getUserId());
        }

        bookMarkService.deleteBookMark(bookMarkInfo);

        if (logger.isDebugEnabled()) {
            logger.debug("bookmark info deletion for user id : {} succeeded.", userId.getUserId());
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}
