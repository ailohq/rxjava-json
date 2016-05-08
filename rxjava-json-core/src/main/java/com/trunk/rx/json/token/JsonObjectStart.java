package com.trunk.rx.json.token;

public class JsonObjectStart extends BaseToken {

  protected static final JsonObjectStart INSTANCE = new JsonObjectStart();

  private JsonObjectStart() {
    // do nothing
  }

  @Override
  public boolean isObjectStart() {
    return true;
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
