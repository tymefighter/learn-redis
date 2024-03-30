package com.tymefighter.facetedSearchApp.search;

import com.fasterxml.jackson.databind.JsonNode;

public class SearchUtils {
  public static String getRedisKey(String entityName, String id) {
    return String.format("%s:%s", entityName, id);
  }

  public static String getRedisKey(JsonNode record, String entityName, String idAttribute) {
    return getRedisKey(entityName, record.get(idAttribute).asText());
  }
}
