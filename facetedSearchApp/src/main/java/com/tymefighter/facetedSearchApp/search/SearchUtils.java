package com.tymefighter.facetedSearchApp.search;

import com.fasterxml.jackson.databind.JsonNode;

public class SearchUtils {
  public static String getRedisKey(String entityName, String id) {
    return String.format("%s:%s", entityName, id);
  }

  public static String getRedisKey(String entityName, String idAttribute, JsonNode record) {
    return getRedisKey(entityName, record.get(idAttribute).asText());
  }

  public static String getRedisSetKey(String entityName, String attribute, String value) {
    return String.format("%s:%s:%s", entityName, attribute, value);
  }

  public static String getRedisSetKey(String entityName, String attribute, JsonNode record) {
    return getRedisSetKey(entityName, attribute, record.get(attribute).asText());
  }
}
