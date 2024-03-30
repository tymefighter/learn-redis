package com.tymefighter.facetedSearchApp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymefighter.facetedSearchApp.data.Event;
import com.tymefighter.facetedSearchApp.data.Events;
import com.tymefighter.facetedSearchApp.search.FacetedSearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

@Component
@Order(0)
public class FacetedSearchAppCLIRunner implements CommandLineRunner {

  private final JedisPool jedisPool;

  private final FacetedSearchService facetedSearchService;

  public FacetedSearchAppCLIRunner(JedisPool jedisPool, FacetedSearchService facetedSearchService) {
    this.jedisPool = jedisPool;
    this.facetedSearchService = facetedSearchService;
  }

  private void clearRedisData() {
    try(Jedis jedis = jedisPool.getResource()) {
      String redisKeyPattern = String.format(
          "%s:*",
          AppConstants.FACETED_SEARCH_ENTITY_NAME
      );

      String cursor = ScanParams.SCAN_POINTER_START;
      ScanParams scanParams = new ScanParams()
          .match(redisKeyPattern);

      do {
        ScanResult<String> scanResult = jedis.scan(cursor, scanParams);

        List<String> keys = scanResult.getResult();
        keys.forEach(jedis::del);

        cursor = scanResult.getCursor();
      } while(!cursor.equals(ScanParams.SCAN_POINTER_START));
    }
  }

  private void insertEvents() {
    ObjectMapper objectMapper = new ObjectMapper();

    Event[] events = Events.getEvents();
    List<JsonNode> eventRecords = Arrays
        .stream(events)
        .map(event -> objectMapper.convertValue(event, JsonNode.class))
        .toList();

    facetedSearchService.insert(eventRecords);
  }

  @Override
  public void run(String... args) {
    clearRedisData();

    insertEvents();

    Scanner scanner = new Scanner(System.in);

    Map<String, String> filter = new HashMap<String, String>();

    while(true) {
      String line = scanner.nextLine();
      String[] lineSplit = line.split(" ");

      switch(lineSplit[0]) {
        case "attr": {
          String key = lineSplit[1];
          String value = lineSplit[2];

          filter.put(key, value);

          break;
        }

        case "search": {
          List<JsonNode> searchedRecords = facetedSearchService.get(filter);
          searchedRecords.forEach(
              eventRecord -> {
                System.out.println(eventRecord.toString());
              }
          );

          return;
        }
      }
    }
  }
}
