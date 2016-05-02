package com.trunk.rx.json.token;

public class JsonArrayStart implements JsonToken {

  protected static final JsonArrayStart INSTANCE = new JsonArrayStart();

  private JsonArrayStart() {
    // do nothing
  }

  @Override
  public String value() {
    return "[";
  }

  @Override
  public String toString() {
    return "JsonArrayStart{}";
  }
}
