package com.trunk.rx.json.token;

public class JsonNull extends BaseToken {

  public static final JsonNull INSTANCE = new JsonNull();

  private JsonNull() {
    // do nothing
  }

  @Override
  public boolean isNull() {
    return true;
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
