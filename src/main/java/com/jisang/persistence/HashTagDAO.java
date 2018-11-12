package com.jisang.persistence;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.jisang.domain.HashTag;

public interface HashTagDAO extends ElasticsearchRepository<HashTag, String>, ElasticsearchHashTagRepository {
    List<HashTag> findByProductId(int productId);
}
