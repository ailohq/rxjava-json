package com.trunk.rx.json.token;

import java.util.Objects;

public class JsonBoolean extends BaseToken {
  private final String value;

  private static JsonBoolean TRUE = new JsonBoolean("true");
  private static JsonBoolean FALSE = new JsonBoolean("false");

  public static JsonBoolean True() {
    return TRUE;
  }

  public static JsonBoolean False() {
    return FALSE;
  }

  public static JsonBoolean of(boolean b) {
    return b ? TRUE : FALSE;
  }

  private JsonBoolean(String value) {
    this.value = value;
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JsonBoolean that = (JsonBoolean) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "JsonBoolean{value='" + value + "'}";
  }
}
