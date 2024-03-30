package com.tymefighter.facetedSearchApp.search.setIntersectionSearchService;

import java.util.Set;

public record SetIntersectionSearchServiceConfig(String entityName, String idAttribute, Set<String> lookupAttributes) {}
