package com.jisang.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import redis.clients.jedis.JedisPoolConfig;

import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.jisang.persistence")
public class DataSourceConfig {

    // MySQL Fields
    // ==========================================================================================================================

    @Value("${datasource.driver.class.name}")
    String driverClassName;
    @Value("${datasource.url}")
    String url;
    @Value("${datasource.user.name}")
    String username;
    @Value("${datasource.user.password}")
    String password;
    @Value("${connectionpool.max-total}")
    int maxTotal;
    @Value("${connectionpool.max-idle}")
    int maxIdle;
    @Value("${connectionpool.min-idle}")
    int minIdle;
    @Value("${connectionpool.max-waitmillis}")
    int maxWaitMillis;

    // Redis Fields
    // ==========================================================================================================================

    @Value("${redis.server.host}")
    private String redisHost;
    @Value("${redis.server.port}")
    private int redisPort;
    @Value("${redis.client.password}")
    private String redisPassword;
    @Value("${redis.connectionpool.max-total}")
    private int redisMaxTotal;
    @Value("${redis.connectionpool.max-idle}")
    private int redisMaxIdle;

    // ES Fields
    // ==========================================================================================================================

    @Value("${elasticsearch.server.host}")
    private String elasticsearchHost;
    @Value("${elasticsearch.server.port}")
    private int elasticsearchPort;
    @Value("${elasticsearch.server.clustername}")
    private String elasticsearchClusterName;

    // MySQL config
    // ==========================================================================================================================

    @Bean
    DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaxTotal(maxTotal);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWaitMillis(maxWaitMillis);

        return dataSource;
    }

    // Redis config
    // ==========================================================================================================================

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfig.setPassword(RedisPassword.of(redisPassword));
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisMaxTotal);
        poolConfig.setMaxIdle(redisMaxIdle);

        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
                .build();

        return new JedisConnectionFactory(redisConfig, clientConfig);

    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setEnableTransactionSupport(true);

        template.setValueSerializer(stringRedisSerializer());
        template.setKeySerializer(stringRedisSerializer());
        template.setHashKeySerializer(stringRedisSerializer());
        template.setHashValueSerializer(stringRedisSerializer());

        return template;
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(jedisConnectionFactory());
        stringRedisTemplate.setEnableTransactionSupport(true);
        return stringRedisTemplate;
    }

    @Bean
    Jackson2HashMapper jackson2HashMapper() {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.disableDefaultTyping();
        objMapper.setSerializationInclusion(Include.NON_NULL);
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2HashMapper(objMapper, true);
    }

    // Elasticsearch config
    // ==========================================================================================================================

    @Bean
    public TransportClient client() throws UnknownHostException {
        Settings elasticsearchSettings = Settings.builder().put("cluster.name", elasticsearchClusterName).build();

        TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);
        client.addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(elasticsearchHost), elasticsearchPort));
        return client;
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate() throws Exception {
        return new ElasticsearchTemplate(client());
    }
}
