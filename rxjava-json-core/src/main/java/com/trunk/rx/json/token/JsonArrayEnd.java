package com.trunk.rx.json.token;

public class JsonArrayEnd implements JsonToken {

  protected static final JsonArrayEnd INSTANCE = new JsonArrayEnd();

  private JsonArrayEnd() {
    // do nothing
  }

  @Override
  public String value() {
    return "]";
  }

  @Override
  public String toString() {
    return "JsonArrayEnd{}";
  }
}
