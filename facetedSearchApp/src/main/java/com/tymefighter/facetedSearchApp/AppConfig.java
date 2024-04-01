package com.tymefighter.facetedSearchApp;

import com.tymefighter.facetedSearchApp.search.fullScanSearchService.FullScanSearchServiceConfig;
import com.tymefighter.facetedSearchApp.search.prefixHashSearchService.PrefixHashServiceConfig;
import com.tymefighter.facetedSearchApp.search.setIntersectionSearchService.SetIntersectionSearchServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class AppConfig {
  // Jedis pool parameters
  private static int MAX_NUM_JEDIS_INSTANCES = 64;
  private static String JEDIS_HOST = "localhost";
  private static int JEDIS_PORT = 6379;

  private static String FACETED_SEARCH_ENTITY_ID_ATTRIBUTE = "id";

  @Bean
  public JedisPool getJedisPool() {
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setJmxEnabled(false);
    jedisPoolConfig.setMaxTotal(MAX_NUM_JEDIS_INSTANCES);
    jedisPoolConfig.setMaxIdle(MAX_NUM_JEDIS_INSTANCES);

    JedisPool jedisPool = new JedisPool(jedisPoolConfig, JEDIS_HOST, JEDIS_PORT);

    return jedisPool;
  }

  @Bean
  public FullScanSearchServiceConfig getFullScanSearchServiceConfig() {
    return new FullScanSearchServiceConfig(AppConstants.FACETED_SEARCH_ENTITY_NAME,
        FACETED_SEARCH_ENTITY_ID_ATTRIBUTE);
  }

  @Bean
  public SetIntersectionSearchServiceConfig getSetIntersectionSearchServiceConfig() {
    Set<String> lookupAttributes = new HashSet<>();
    lookupAttributes.add("type");
    lookupAttributes.add("country");
    lookupAttributes.add("year");

    return new SetIntersectionSearchServiceConfig(AppConstants.FACETED_SEARCH_ENTITY_NAME,
        FACETED_SEARCH_ENTITY_ID_ATTRIBUTE, lookupAttributes);
  }

  @Bean
  public PrefixHashServiceConfig getPrefixHashServiceConfig() {
    List<String> lookupAttributes = new ArrayList<>();
    lookupAttributes.add("type");
    lookupAttributes.add("country");
    lookupAttributes.add("year");

    return new PrefixHashServiceConfig(AppConstants.FACETED_SEARCH_ENTITY_NAME,
      FACETED_SEARCH_ENTITY_ID_ATTRIBUTE, lookupAttributes);
  }
}
