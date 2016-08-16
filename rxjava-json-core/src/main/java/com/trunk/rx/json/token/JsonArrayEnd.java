package com.trunk.rx.json.token;

public class JsonArrayEnd extends BaseToken {

  private static final JsonArrayEnd INSTANCE = new JsonArrayEnd();

  public static JsonArrayEnd instance() {
    return INSTANCE;
  }

  private JsonArrayEnd() {
    // do nothing
  }

  @Override
  public boolean isArrayEnd() {
    return true;
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
