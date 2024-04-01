package com.tymefighter.facetedSearchApp.search.prefixHashSearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymefighter.facetedSearchApp.search.FacetedSearchService;
import com.tymefighter.facetedSearchApp.search.SearchUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

@Service
@Primary
public class PrefixHashSearchService implements FacetedSearchService {
  private final JedisPool jedisPool;

  private final PrefixHashServiceConfig config;

  public PrefixHashSearchService(final JedisPool jedisPool,
                                 final PrefixHashServiceConfig config) {
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

            List<String> lookupAttributes = config.lookupAttributes();
            for(int prefixIndex = 0; prefixIndex < lookupAttributes.size(); prefixIndex += 1) {
              List<Map.Entry<String, String>> attributeValues = lookupAttributes
                  .subList(0, prefixIndex + 1)
                  .stream()
                  .map(
                      lookupAttribute -> (Map.Entry<String, String>) new AbstractMap.SimpleEntry<String, String>(
                          lookupAttribute,
                          record.get(lookupAttribute).asText()
                      )
                  )
                  .toList();

              String redisSetHashKey = SearchUtils.getRedisSetHashKey(
                  config.entityName(),
                  attributeValues
              );

              // Add Redis key to set with prefix hash
              jedis.sadd(redisSetHashKey, redisKey);
            }
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
      List<Map.Entry<String, String>> attributeValues = new ArrayList<>();

      for(String lookupAttribute : config.lookupAttributes()) {
        String lookupValue = filter.get(lookupAttribute);

        if(lookupValue == null) {
          break;
        }

        attributeValues.add(new AbstractMap.SimpleEntry<>(
            lookupAttribute, lookupValue));
      }

      String redisSetHashKey = SearchUtils.getRedisSetHashKey(config.entityName(),
          attributeValues);

      String cursor = ScanParams.SCAN_POINTER_START;
      ObjectMapper objectMapper = new ObjectMapper();
      do {
        ScanResult<String> scanResult = jedis.sscan(redisSetHashKey, cursor);
        List<String> redisKeys = scanResult.getResult();

        for(String redisKey : redisKeys) {
          String jsonValue = jedis.get(redisKey);

          try {
            JsonNode record = objectMapper.readTree(jsonValue);
            records.add(record);
          } catch(JsonProcessingException exception) {
            System.err.println("Error occurred while parsing record to JSON:");
            exception.printStackTrace();
          }
        }

        cursor = scanResult.getCursor();
      } while(!cursor.equals(ScanParams.SCAN_POINTER_START));
    }

    return records;
  }

  private boolean validateFilter(Map<String, String> filter) {
    List<String> lookupAttributes = config.lookupAttributes();

    // The fields in the filter must be a subset of the lookup attributes
    for(String filterField : filter.keySet()) {
      if(!lookupAttributes.contains(filterField)) {
        return false;
      }
    }

    // The filter must represent a prefix of the lookup attributes
    boolean valueNotPresentForPrevAttribute = false;
    for(String lookupAttribute : lookupAttributes) {
      if(valueNotPresentForPrevAttribute && filter.containsKey(lookupAttribute)) {
        return false;
      }

      if(!filter.containsKey(lookupAttribute)) {
        valueNotPresentForPrevAttribute = true;
      }
    }

    return true;
  }
}
