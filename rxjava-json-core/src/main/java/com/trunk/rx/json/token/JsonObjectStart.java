package com.trunk.rx.json.token;

public class JsonObjectStart implements JsonToken {

  protected static final JsonObjectStart INSTANCE = new JsonObjectStart();

  private JsonObjectStart() {
    // do nothing
  }

  @Override
  public String value() {
    return "{";
  }

  @Override
  public String toString() {
    return "JsonObjectStart{}";
  }
}
