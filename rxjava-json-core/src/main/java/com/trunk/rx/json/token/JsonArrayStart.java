package com.trunk.rx.json.token;

public class JsonArrayStart extends BaseToken {

  protected static final JsonArrayStart INSTANCE = new JsonArrayStart();

  private JsonArrayStart() {
    // do nothing
  }

  @Override
  public boolean isArrayStart() {
    return true;
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
