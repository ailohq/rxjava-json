package com.trunk.rx.json.token;

import java.util.Objects;

public class JsonName implements JsonToken {
  private final String value;

  public static JsonName of(String name) {
    return new JsonName(name);
  }

  private JsonName(String value) {
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
    JsonName jsonName = (JsonName) o;
    return Objects.equals(value, jsonName.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "JsonName{value='" + value + "'}";
  }
}
