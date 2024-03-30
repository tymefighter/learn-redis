package com.tymefighter.facetedSearchApp;

import com.tymefighter.facetedSearchApp.search.fullScanSearchService.FullScanSearchServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class AppConfig {
  private static int MAX_NUM_JEDIS_INSTANCES = 64;
  private static String JEDIS_HOST = "localhost";
  private static int JEDIS_PORT = 6379;

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
    return new FullScanSearchServiceConfig("international_event", "id");
  }
}
