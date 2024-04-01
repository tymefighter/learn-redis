package com.tymefighter.facetedSearchApp.search.prefixHashSearchService;

import java.util.List;

public record PrefixHashServiceConfig(
    String entityName,
    String idAttribute,
    List<String> lookupAttributes
) {}
