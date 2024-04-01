package com.tymefighter.facetedSearchApp.search;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SearchUtils {
  public static String getRedisKey(String entityName, String id) {
    return String.format("%s:%s", entityName, id);
  }

  public static String getRedisKey(String entityName, String idAttribute, JsonNode record) {
    return getRedisKey(entityName, record.get(idAttribute).asText());
  }

  public static String getRedisSetKey(String entityName, String attribute, String value) {
    return String.format("%s:set:%s:%s", entityName, attribute, value);
  }

  public static String getRedisSetKey(String entityName, String attribute, JsonNode record) {
    return getRedisSetKey(entityName, attribute, record.get(attribute).asText());
  }

  public static String getRedisSetHashKey(String entityName, List<Map.Entry<String, String>> attributeValues) {
    try {
      List<String> attrStrList = attributeValues
          .stream()
          .map(
              entrySet -> String.format("(%s,%s)", entrySet.getKey(), entrySet.getValue())
          )
          .toList();

      String attrStr = String.join(",", attrStrList);
      byte[] attrStrBytes = attrStr.getBytes("UTF-8");

      byte[] hashBytes = MessageDigest
          .getInstance("MD5")
          .digest(attrStrBytes);

      StringBuilder stringBuilder = new StringBuilder();
      for(byte hashByte : hashBytes) {
        stringBuilder.append(String.format("%02x", hashByte));
      }

      String hash = stringBuilder.toString();

      return String.format("%s:setHash:%s", entityName, hash);
    } catch(UnsupportedEncodingException unsupportedEncodingException) {
      System.err.println("UnsupportedEncodingException while generating redis hash key:");
      unsupportedEncodingException.printStackTrace();
    } catch(NoSuchAlgorithmException noSuchAlgorithmException) {
      System.err.println("NoSuchAlgorithmException while generating redis hash key:");
      noSuchAlgorithmException.printStackTrace();
    }

    return null;
  }
}
