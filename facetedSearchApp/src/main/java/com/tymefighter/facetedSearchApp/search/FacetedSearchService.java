package com.tymefighter.facetedSearchApp.search;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface FacetedSearchService {
  public void insert(List<JsonNode> records);

  public List<JsonNode> get(Map<String, String> filter);
}
