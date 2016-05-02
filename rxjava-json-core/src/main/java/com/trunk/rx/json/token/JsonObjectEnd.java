package com.trunk.rx.json.token;

public class JsonObjectEnd implements JsonToken {

  protected static final JsonObjectEnd INSTANCE = new JsonObjectEnd();

  private JsonObjectEnd() {
    // do nothing
  }

  @Override
  public String value() {
    return "}";
  }

  @Override
  public String toString() {
    return "JsonObjectEnd{}";
  }
}
