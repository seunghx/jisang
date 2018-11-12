package com.jisang.service.bookmark;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.jisang.dto.bookmark.BookMarkInfoDTO;
import com.jisang.persistence.BookMarkDAO;
import com.jisang.support.NoSuchMarketException;

@Service
public class BookMarkServiceImpl implements BookMarkService {

    private final Logger logger = LoggerFactory.getLogger(BookMarkServiceImpl.class);

    @Autowired
    private BookMarkDAO bookMarkDAO;

    @Override
    public boolean isBookMarked(BookMarkInfoDTO bookMarkInfo) {
        Objects.requireNonNull(bookMarkInfo,
                "Null value parameter bookMarkInfo detected while trying to check whether bookmark info.");

        int bookMarkCnt = bookMarkDAO.readCount(bookMarkInfo);

        if (bookMarkCnt == 0) {
            logger.debug("Bookmark info doesn't exist. Returning false.");
            return false;
        } else {
            logger.debug("Bookmark info exists. Returning true.");
            return true;
        }
    }

    @Override
    public List<BookMarkInfoDTO> findBookmarks(int userId) {
        return bookMarkDAO.readList(userId);
    }

    /**
     * 특정 마켓에 대한 유저의 북마크(좋아요)값을 설정 혹은 해제 한다. 아래 메서드는 토글 방식으로 수행된다.
     * 
     * 북마크 정보 추가({@code bookMarkDAO.create(bookMarkInfo}) 중
     * {@link DataIntegrityViolationException}이 발생할 경우 아래 메서드는 해당 예외를
     * {@link NoSuchMarketException}로 포장하여 던진다.
     * 
     * {@link DataIntegrityViolationException}을 그대로 사용해도 되나 그렇게 하지 않은 이유는 다른 원인을
     * 기반으로 이 예외가 던져질 수도 있기 때문이다. 마켓 id에 대하여
     * {@link DataIntegrityViolationException}을 사용할 경우 후에 {@link BookMark} 관련 로직이
     * 추가되어 또 다른 foreign key 제약 조건 위반 요소가 추가되었다고 할 때의 변경 요소가 현재와 같이
     * {@link NoSuchMarketException}을 던지는 방법보다 많다는 단점이 있으며
     * {@link NoSuchMarketException}와 같은 예외를 정의하면 다른 도메인 관련 비즈니스 로직에서도 사용할 수 있다는 장점이
     * 있다. 또한 특정 컨트롤러 클래스에서 예외를 처리하게 하지 않고 {@link @ControllerAdvice} 클래스에 해당 예외에 대한
     * 처리 로직을 등록하여 공통 처리하기에도 더 좋다. {@link DataIntegrityViolationException}의 경우 다른
     * 도메인에서도 같은 예외가 던져질 수 있기 때문에 공통 처리하기에 좋지 않다.
     * 
     * 현재 구현은 DAO에서 존재하지 않는 마켓 id가 발견 되었을 경우 예외가 던져지면 이를 아래 메서드가 잡아 적절한 응답을 취하는
     * 형태이다. 이 경우 응답은 400 - Bad Request가 적절할 것이다. 그런데 400 - Bad Request 하면 핸들러 메서드로의
     * 바인딩 과정 중 수행되는 bean validation이 떠오른다. 이로부터 차라리 바인딩 과정 중 실제 존재하는 마켓 id인지에 대한
     * 검사를 수행하는 방법도 생각해보았다.
     * 
     * 그러나 이 경우 데이터베이스 서버를 한 번 더 방문하게 된다는 의미이므로 그렇게하지 않기로 하였다.
     * 
     */
    @Override
    public void modifyBookMark(BookMarkInfoDTO bookMarkInfo) {
        Objects.requireNonNull(bookMarkInfo,
                "Null value parameter bookMarkInfo detected while trying to modifying bookmark info.");

        int bookMarkCnt = bookMarkDAO.readCount(bookMarkInfo);

        if (bookMarkCnt == 0) {
            logger.debug("Parameter {} is safe. Starting to create bookmark info.", bookMarkInfo);
            try {
                bookMarkDAO.create(bookMarkInfo);
            } catch (DataIntegrityViolationException e) {
                logger.info("Exception {} occured while trying to create bookmark info.", e.toString());
                logger.info("Throwing {} by wrapping low level exception {}.", NoSuchMarketException.class,
                        e.toString());

                throw new NoSuchMarketException("Received Non-existing market id.", e, bookMarkInfo.getMarketId());
            }
        } else if (bookMarkCnt == 1) {
            logger.debug("Parameter {} is safe. Starting to delete bookmark info.", bookMarkInfo);
            bookMarkDAO.delete(bookMarkInfo);
        } else {
            logger.error("Illegal state detected. Unique index associated with bookmark might be deleted!!");
            logger.error("Multiple entity(row) count for user id {} and market id {}", bookMarkInfo.getUserId(),
                    bookMarkInfo.getMarketId());
            throw new IllegalStateException("Multiple entity detected for Unique entity.");
        }

    }

    /**
     * 위의 {@link #modifyBookMark} 메서드를 이용해도 북마크 정보에 대한 삭제 로직은 충분히 수행 가능하다. 그러나 토글
     * 방식으로 로직이 수행되는 위의 메서드를 이용해 북마크 정보를 삭제하려고 할 경우 혹시나 네트워크 지연 등의 이유로 북마크 삭제 버튼이 두
     * 번 눌렸을 경우 북마크 정보가 create 될 수 있어 아래 메서드를 정의하였다.
     */
    @Override
    public void deleteBookMark(BookMarkInfoDTO bookMarkInfo) {

        Objects.requireNonNull(bookMarkInfo,
                "Null value argument bookMarkInfo detected while trying to delete bookmark info.");

        logger.debug("Parameter {} is safe. Now starting to delete bookmark info.");

        bookMarkDAO.delete(bookMarkInfo);

    }

}
