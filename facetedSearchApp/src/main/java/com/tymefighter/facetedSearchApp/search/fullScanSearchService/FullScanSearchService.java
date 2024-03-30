package com.tymefighter.facetedSearchApp.search.fullScanSearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymefighter.facetedSearchApp.search.FacetedSearchService;
import com.tymefighter.facetedSearchApp.search.SearchUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FullScanSearchService implements FacetedSearchService {

  private JedisPool jedisPool;
  private FullScanSearchServiceConfig config;

  public FullScanSearchService(JedisPool jedisPool, FullScanSearchServiceConfig config) {
    this.jedisPool = jedisPool;
    this.config = config;
  }

  @Override
  public void insert(List<JsonNode> records) {
    try(Jedis jedis = jedisPool.getResource()) {
      records.forEach(
          record -> {
            String redisKey = SearchUtils.getRedisKey(record, config.entityName(), config.idAttribute());
            String jsonValue = record.toString();

            jedis.set(redisKey, jsonValue);
          }
      );
    }
  }

  @Override
  public List<JsonNode> get(Map<String, String> filter) {
    List<Map.Entry<String, String>> filterFields = new ArrayList<Map.Entry<String, String>>(filter.entrySet());

    ArrayList<JsonNode> records = new ArrayList<JsonNode>();

    try(Jedis jedis = jedisPool.getResource()) {
      String redisKeyPattern = SearchUtils.getRedisKey(config.entityName(), "*");
      ScanParams scanParams = new ScanParams()
          .match(redisKeyPattern);

      ObjectMapper objectMapper = new ObjectMapper();

      String cursor = ScanParams.SCAN_POINTER_START;
      do {
        ScanResult<String> scanResult = jedis.scan(cursor, scanParams);

        List<String> keys = scanResult.getResult();
        keys.forEach(key -> {
          try {
            String jsonValue = jedis.get(key);
            JsonNode record = objectMapper.readTree(jsonValue);

            if (matchesFilter(filterFields, record)) {
              records.add(record);
            }
          } catch(JsonProcessingException exception) {
            System.err.println("Error occurred while parsing record to JSON:");
            exception.printStackTrace();
          }
        });

        cursor = scanResult.getCursor();
      } while(!cursor.equals(ScanParams.SCAN_POINTER_START));
    }

    return records;
  }

  private static boolean matchesFilter(List<Map.Entry<String, String>> filterFields, JsonNode record) {
    for(Map.Entry<String, String> filterField : filterFields) {
      if(!record
          .get(filterField.getKey())
          .asText()
          .equals(filterField.getValue())) {
        return false;
      }
    }

    return true;
  }
}
