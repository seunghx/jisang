package com.jisang.persistence;

import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.springframework.retry.annotation.Retryable;

import com.jisang.domain.HashTag;

public interface ElasticsearchHashTagRepository {
    /**
     * 이 메서드(삭제 연산) 실행 중 예외가 발생하였을 경우 최대 두번의 재시도를 수행한다.
     */
    @Retryable(value = { ElasticsearchException.class }, maxAttempts = 2)
    public void deleteAllByProductId(int productId);

    public List<HashTag> searchByContent(String content);
}
