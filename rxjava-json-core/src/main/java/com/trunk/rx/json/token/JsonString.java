package com.trunk.rx.json.token;

import java.util.Objects;

public class JsonString implements JsonToken {
  private final String value;

  public static JsonString of(String name) {
    return new JsonString(name);
  }

  private JsonString(String value) {
    this.value = value;
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
    JsonString that = (JsonString) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "JsonString{value='" + value + "'}'";
  }
}
