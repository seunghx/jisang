package com.jisang.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.jisang.persistence.HashTagDAO;
import com.jisang.persistence.MultipartDAO;
import com.jisang.persistence.ProductDAO;

/**
 * 
 * 지상 어플리케이션에 필요한 스케줄링 관련 메서드는 다 이 클래스 아래에 두려고 한다. 현재 구현으로는 아래
 * {@link #refreshProductHit()}와 AWS S3 상의 이미지와 elasticsearch에 저장된 (검색어 미리보기로
 * 사용되는) 해시태그 정보 중 예외 발생 등의 이유로 삭제되지 않은 정보를 주기적으로 삭제하는 메서드
 * {@link #deleteImages(), #deleteHashTags()}가 존재한다.
 * 
 * 
 * @author leeseunghyun
 *
 */
@Component
public class Scheduler {

    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    private ProductDAO productDAO;
    @Autowired
    private MultipartDAO multipartDAO;
    @Autowired
    private HashTagDAO hashTagDAO;

    @Autowired
    @Qualifier("imageTrashCan")
    private List<String> imageTrashCan;
    @Autowired
    @Qualifier("hashtagTrashCan")
    private List<Integer> hashtagTrashCan;

    /**
     * 특정 기간마다 상품 조회수 정보를 업데이트 해줄 필요가 있다. 지상 어플리케이션에는 베스트 상품 목록 화면이 존재하는데, 상품 조회수를
     * 업데이트 해주지 않을 경우 17FW의 인기 제품이 18SS의 베스트 상품 뷰에 여전히 남아있을 수 있다.
     */
    @Scheduled(cron = "${schedule.product-hit-refresh.cron-expression}")
    public void refreshProductHit() {
        logger.info("Starting to refresh product hit.");

        productDAO.refreshHit();

        logger.info("Refreshing product hit succeeded.");
    }

    /**
     * S3 등의 스토리지로부터 이미지 파일 삭제에 실패하였을 경우 삭제 실패 된 이미지들은 {@code imageTrashCan}에 담긴다.
     * 아래 메서드는 정해진 시간마다 {@code imageTrashCan}에 담긴 이미지 url에 해당하는 이미지를 삭제한다.
     */
    @Scheduled(cron = "${schedule.delete-images.cron-expression}")
    public void deleteImages() {
        logger.info("Starting to delete images from cloud storage");

        synchronized (imageTrashCan) {
            try {
                multipartDAO.delete(imageTrashCan.toArray(new String[imageTrashCan.size()]));
            } catch (AmazonClientException e) {
                logger.error("Despite of retried call, Deleting images from cloud storage failed...");
                logger.error("Manual deletion required. Manual deletion required images : {}", imageTrashCan);

                logger.error("Exception : {}", e);
            }

            imageTrashCan.clear();
        }

        logger.info("Deleting images succeeded.");
    }

    @Scheduled(cron = "${schedule.delete-hashtags.cron-expression}")
    public void deleteHashTags() {
        logger.info("Starting to delete hashtags from elasticsearch.");

        synchronized (hashtagTrashCan) {
            hashtagTrashCan.stream().forEach(productId -> {
                try {
                    hashTagDAO.deleteAllByProductId(productId);
                } catch (ElasticsearchException e) {
                    logger.error("Exception occurred while trying to delete hashtags from elasticsearch.");
                    logger.error("Exception : {}", e);
                }
            });

        }
    }

}
