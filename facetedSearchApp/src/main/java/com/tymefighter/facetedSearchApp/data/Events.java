package com.tymefighter.facetedSearchApp.data;

public class Events {
  private static Event[] events = new Event[] {
      Event.createEvent("IPL", EventType.SPORTS, "India", 2024),
      Event.createEvent("AFC Asian Cup", EventType.SPORTS, "Qatar", 2024),
      Event.createEvent("Bahrain GP", EventType.MOTORSPORTS, "Bahrain", 2023),
      Event.createEvent("MotoGP Bharat", EventType.MOTORSPORTS, "India", 2023),
      Event.createEvent("FIFA 22", EventType.SPORTS, "Qatar", 2022),
      Event.createEvent("Wimbledon 22", EventType.SPORTS, "England", 2022),
      Event.createEvent("Tomorrowland", EventType.CONCERT, "Thailand", 2026),
      Event.createEvent("UAE SWAT Challenge", EventType.OTHER, "UAE", 2024),
      Event.createEvent("Day of Reckoning - Boxing", EventType.SPORTS, "Saudi Arabia", 2023),
      Event.createEvent("WMF Muaythai", EventType.SPORTS, "Thailand", 2024),
      Event.createEvent("Dubai Horse Racing World Cup", EventType.SPORTS, "UAE", 2024),
      Event.createEvent("Sunburn", EventType.CONCERT, "India", 2024),
  };

  public static Event[] getEvents() {
    return events;
  }
}
