package com.jisang.service.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jisang.domain.Comment;
import com.jisang.dto.comment.CommentResponseDTO;
import com.jisang.dto.comment.CommentRegisterDTO;
import com.jisang.persistence.CommentDAO;
import com.jisang.persistence.UserDAO;
import com.jisang.support.NestedCommentException;
import com.jisang.support.NoSuchParentCommentIdException;
import com.jisang.support.NoSuchProductException;

/**
 * 
 * {@link Comment} 도메인 관련 비즈니스 로직을 수행하는 서비스 오브젝트이다.
 *
 * 
 * @author leeseunghyun
 *
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CommentDAO commentDAO;
    @Autowired
    private UserDAO userDAO;

    /**
     * 
     * 댓글 등록 로직을 수행한다. 이 메서드의 초반부에는 {@code commentDTO.parentId}에 대한 검사가 수행되고 있음을 알 수
     * 있다. 이런 검사를 수행하는 이유는 {@link NoSuchParentCommentIdException} 주석에서 다루었듯이 데이터베이스의
     * tbl_comments 테이블의 comment_parent_id 칼럼에 foreign key를 (self reference) 지정하지
     * 않았기 때문에 안전한 데이터의 삽입(안전이라기보단 올바르지 않은 데이터가 저장되는 것을 방지)이 가능하기 위함이다.
     * 
     * 또한 현재 구현은 댓글의 깊이가 2단계로 한정하여 구현이되어 있다. 이런 구조의 경우 후에 대댓글에 대한 댓글도 가능하다는 요구사항이
     * 추가될 경우 메서드의 변경이 필요하다. 후에 코드를 수정해야겠다.
     * 
     * 존재하지 않는 상품 id가 입력될 경우 {@link DataIntegrityException}이 발생하며 이 예외는 보다 구체적인 예외인
     * {@link NoSuchProductException} 으로 변환된다. 이렇게 구체적인 예외를 사용할 경우 후에 이 클래스가 구현하는
     * {@link CommentService}에 로직이 추가되어 기존에는 없던 또 다른
     * {@link DataIntegrityViolationException}을 발생시킬 요소가 추가 될 경우에도 변경이 필요없다. 또한 특정
     * 컨트롤러 클래스에서 예외를 처리하게 하지 않고 {@link @ControllerAdvice} 클래스에 해당 예외에 대한 처리 로직을
     * 등록하여 공통 처리하기에도 더 좋다. {@link DataIntegrityViolationException}의 경우 다른 도메인에서도 같은
     * 예외가 던져질 수 있기 때문에 공통 처리하기에 좋지 않다.
     * 
     */
    @Override
    @Transactional
    public void registerComment(CommentRegisterDTO commentDTO) {
        Objects.requireNonNull(commentDTO, "Null value argument commentDTO detected while trying to register comment.");

        if (commentDTO.getParentId() != 0) {
            Comment parent = commentDAO.read(commentDTO.getParentId());

            if (parent == null) {
                logger.info("Invalid parent comment id detected. Parent comment id in {} does not exist.", commentDTO);
                throw new NoSuchParentCommentIdException(
                        "Non existing parent comment id detected while trying to register comment",
                        commentDTO.getParentId());
            } else if (parent.getParentId() != parent.getId()) {
                logger.warn("Illegal access detected with comment hierarchy. Maximum comment hierarchy depth is 2.");

                throw new NestedCommentException(
                        "Illegal comment hierarchy detected while tryping to register comment");
            }
        }

        logger.debug("Checking parent comment completed.");

        Comment comment = modelMapper.map(commentDTO, Comment.class);

        try {
            commentDAO.create(comment);

            if (comment.getParentId() == 0)
                commentDAO.updateParentId(comment.getId());

        } catch (DataIntegrityViolationException e) {
            logger.info(
                    "Exception {} occured while trying to register comment. It is caused by non-existing product id.",
                    e.toString());
            logger.info("Throwing {} by wrapping {}.", NoSuchProductException.class, e.getClass().getName());

            throw new NoSuchProductException("Received non existing product id.", e, commentDTO.getProductId());
        }
    }

    @Override
    public List<CommentResponseDTO> findCommentList(int productId) {
        List<Comment> commentList = commentDAO.readList(productId);

        logger.debug("Read comment list for product id : {} succeeded.", productId);

        Map<Integer, CommentResponseDTO> commentMap = new HashMap<>();

        commentList.stream().filter(comment -> comment.getParentId() == 0).forEach(comment -> {
            CommentResponseDTO commentDTO = modelMapper.map(comment, CommentResponseDTO.class);
            modelMapper.map(userDAO.read(comment.getUserId()), commentDTO);

            commentMap.put(comment.getId(), commentDTO);
        });

        commentList.stream().filter(comment -> comment.getParentId() != 0).forEach(child -> {
            CommentResponseDTO parent = commentMap.get(child.getParentId());

            if (parent == null) {
                logger.error("Illegal state related to self reference of comment table in RDB, detected.");
                logger.error("Invalid parent comment id does not allowed by comment registering method of this class.");
                logger.error("So it might be foreign key constraint problem. Checking database required!!.");

                throw new IllegalStateException("Odd foreign key constraint related to comment, detected.");
            } else {
                CommentResponseDTO commentDTO = modelMapper.map(child, CommentResponseDTO.class);
                modelMapper.map(userDAO.read(child.getUserId()), commentDTO);

                parent.getChilds().add(commentDTO);
            }
        });

        commentMap.values().forEach(parent -> parent.getChilds()
                .sort((child1, child2) -> (-1) * child1.getUploadTime().compareTo(child2.getUploadTime())));

        List<CommentResponseDTO> returnedList = new ArrayList<>(commentMap.values());
        returnedList.sort((parent1, parent2) -> (-1) * parent1.getUploadTime().compareTo(parent2.getUploadTime()));

        return returnedList;
    }

}
