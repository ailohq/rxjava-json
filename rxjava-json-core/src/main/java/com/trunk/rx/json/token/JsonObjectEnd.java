package com.trunk.rx.json.token;

public class JsonObjectEnd extends BaseToken {

  protected static final JsonObjectEnd INSTANCE = new JsonObjectEnd();

  private JsonObjectEnd() {
    // do nothing
  }

  @Override
  public boolean isObjectEnd() {
    return true;
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
