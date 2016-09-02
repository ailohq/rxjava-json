package com.trunk.rx.json.token;

public class JsonObjectEnd extends BaseToken {

  private static final class Holder {
    private static final JsonObjectEnd INSTANCE = new JsonObjectEnd();
  }

  public static JsonObjectEnd instance() {
    return Holder.INSTANCE;
  }

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
