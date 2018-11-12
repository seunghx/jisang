package com.jisang.persistence;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import com.jisang.domain.HashTag;

public class ElasticsearchHashTagRepositoryImpl implements ElasticsearchHashTagRepository {

    private final Logger logger = LoggerFactory.getLogger(ElasticsearchHashTagRepositoryImpl.class);

    @Autowired
    private TransportClient transportClient;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public void deleteAllByProductId(int productId) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(transportClient);

        builder.filter(QueryBuilders.termQuery("productId", productId)).source("product")
                .execute(new ActionListener<BulkByScrollResponse>() {

                    @Override
                    public void onResponse(BulkByScrollResponse response) {
                        long deletedNum = response.getDeleted();

                        logger.info("Deleting hashtags for product id {} of index {} succeeded from Elasticsearch.",
                                productId, "product");
                        logger.info("Deleted hashtags number : {}.", deletedNum);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logger.error("An Exception occurred while trying to delete hashtags. Exception : {}",
                                e.toString());
                        logger.error(
                                "Manual deletion required. Hashtags must be deleted : product id : {} of index {}.",
                                productId, "product");
                        throw new ElasticsearchException("Deleting hashtags from elasticsearch failed.", e);
                    }
                });
    }

    @Override
    public List<HashTag> searchByContent(String content) {
        Objects.requireNonNull(content, "Content used to search hashtag is null.");

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("content", content)).build();

        return esTemplate.queryForList(searchQuery, HashTag.class);
    }

}
