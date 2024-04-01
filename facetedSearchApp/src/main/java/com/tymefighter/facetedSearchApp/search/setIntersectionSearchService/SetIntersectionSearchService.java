package com.tymefighter.facetedSearchApp.search.setIntersectionSearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymefighter.facetedSearchApp.search.FacetedSearchService;
import com.tymefighter.facetedSearchApp.search.SearchUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SetIntersectionSearchService implements FacetedSearchService {

  private final JedisPool jedisPool;
  private final SetIntersectionSearchServiceConfig config;

  public SetIntersectionSearchService(JedisPool jedisPool, SetIntersectionSearchServiceConfig config) {
    this.jedisPool = jedisPool;
    this.config = config;
  }

  @Override
  public void insert(List<JsonNode> records) {
    try(Jedis jedis = jedisPool.getResource()) {
      records.forEach(
          record -> {
            String redisKey = SearchUtils.getRedisKey(config.entityName(), config.idAttribute(), record);
            String jsonValue = record.toString();

            // Insert JSON value corresponding to Redis key
            jedis.set(redisKey, jsonValue);

            config
                .lookupAttributes()
                .forEach(lookupAttribute -> {
                  String redisSetKey = SearchUtils
                      .getRedisSetKey(config.entityName(), lookupAttribute, record);

                  // Add Redis key into the set which represents in lookup attribute value
                  jedis.sadd(redisSetKey, redisKey);
                });
          }
      );
    }
  }

  @Override
  public List<JsonNode> get(Map<String, String> filter) {
    if(!validateFilter(filter)) {
      System.err.println("Invalid filter provided");
      return null;
    }

    ArrayList<JsonNode> records = new ArrayList<JsonNode>();

    try(Jedis jedis = jedisPool.getResource()) {
      String[] redisSetKeys = filter
          .entrySet()
          .stream()
          .map(
              entrySet -> SearchUtils
                  .getRedisSetKey(config.entityName(), entrySet.getKey(), entrySet.getValue())
          )
          .toArray(String[]::new);

      Set<String> filteredRecordRedisKeys = jedis.sinter(redisSetKeys);

      ObjectMapper objectMapper = new ObjectMapper();
      filteredRecordRedisKeys.forEach(
          redisKey -> {
            try {
              String jsonValue = jedis.get(redisKey);
              JsonNode record = objectMapper.readTree(jsonValue);
              records.add(record);
            } catch(JsonProcessingException exception) {
              System.err.println("Error occurred while parsing record to JSON:");
              exception.printStackTrace();
            }
          }
      );
    }

    return records;
  }

  private boolean validateFilter(Map<String, String> filter) {
    Set<String> lookupAttributes = config.lookupAttributes();

    for(String key : filter.keySet()) {
      if(!lookupAttributes.contains(key)) {
        return false;
      }
    }

    return true;
  }
}
