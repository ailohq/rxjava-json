package com.trunk.rx.json.token;

public class JsonColon extends BaseToken {
  private static final JsonColon INSTANCE = new JsonColon();

  public static JsonColon instance() {
    return INSTANCE;
  }

  @Override
  public String value() {
    return ":";
  }
}
