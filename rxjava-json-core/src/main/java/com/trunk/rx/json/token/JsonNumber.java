package com.trunk.rx.json.token;

import java.util.Objects;

public class JsonNumber extends BaseToken {
  private final String value;

  public static JsonNumber of(String value) {
    return new JsonNumber(value);
  }

  private JsonNumber(String value) {
    this.value = value;
  }

  @Override
  public boolean isNumber() {
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
    JsonNumber that = (JsonNumber) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "JsonNumber{value='" + value + "'}";
  }
}
