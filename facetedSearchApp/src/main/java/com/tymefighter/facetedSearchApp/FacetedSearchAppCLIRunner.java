package com.tymefighter.facetedSearchApp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymefighter.facetedSearchApp.data.Event;
import com.tymefighter.facetedSearchApp.data.Events;
import com.tymefighter.facetedSearchApp.search.FacetedSearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(0)
public class FacetedSearchAppCLIRunner implements CommandLineRunner {

  private final FacetedSearchService facetedSearchService;

  public FacetedSearchAppCLIRunner(FacetedSearchService facetedSearchService) {
    this.facetedSearchService = facetedSearchService;
  }

  @Override
  public void run(String... args) {
    ObjectMapper objectMapper = new ObjectMapper();

    Event[] events = Events.getEvents();
    List<JsonNode> eventRecords = Arrays
        .stream(events)
        .map(event -> objectMapper.convertValue(event, JsonNode.class))
        .toList();

    facetedSearchService.insert(eventRecords);

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
