package com.trunk.rx.json.token;

import java.util.Objects;

/**
 * A raw, unparsed JSON value that will be emitted as is.
 */
public class JsonRaw extends BaseToken {

  private final String value;

  public static JsonRaw of(String rawString) {
    return new JsonRaw(rawString);
  }

  private JsonRaw(String value) {
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
    JsonRaw that = (JsonRaw) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "JsonRaw{value='" + value + "'}'";
  }

}
