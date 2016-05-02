package com.trunk.rx.json.token;

public class JsonNull implements JsonToken {

  public static final JsonNull INSTANCE = new JsonNull();

  private JsonNull() {
    // do nothing
  }

  @Override
  public String value() {
    return "null";
  }

  @Override
  public String toString() {
    return "JsonNull{}";
  }
}
