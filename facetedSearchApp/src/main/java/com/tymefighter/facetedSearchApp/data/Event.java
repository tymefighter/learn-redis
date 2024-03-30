package com.tymefighter.facetedSearchApp.data;

import java.util.UUID;

public class Event {
  private String id;
  private String name;
  private EventType type;
  private String country;
  private int year;

  public Event() {}

  public Event(
      String id,
      String name,
      EventType type,
      String country,
      int year
  ) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.country = country;
    this.year = year;
  }

  public static Event createEvent(
      String name,
      EventType type,
      String country,
      int year
  ) {
    String id = UUID.randomUUID().toString();

    return new Event(
      id,
      name,
      type,
      country,
      year
    );
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EventType getType() {
    return type;
  }

  public void setType(EventType type) {
    this.type = type;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }
}
