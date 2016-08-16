package com.trunk.rx.json;

import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonToken;

import java.util.Objects;

public class JsonTokenEvent {
  private final JsonToken token;
  private final JsonPath jsonPath;

  public JsonTokenEvent(JsonToken token, JsonPath jsonPath) {
    this.token = token;
    this.jsonPath = jsonPath;
  }

  public JsonToken getToken() {
    return token;
  }

  public JsonPath getJsonPath() {
    return jsonPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JsonTokenEvent that = (JsonTokenEvent) o;
    return Objects.equals(token, that.token) &&
      Objects.equals(jsonPath, that.jsonPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, jsonPath);
  }

  @Override
  public String toString() {
    String sb = "JsonTokenEvent{" + "jsonPath='" + jsonPath + '\'' +
      ", token=" + token +
      '}';
    return sb;
  }
}
