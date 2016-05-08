package com.trunk.rx.json;

import java.util.Collection;
import java.util.Objects;

import com.trunk.rx.json.path.JsonPath;

public class JsonObjectEvent {
  private final JsonPath path;
  private final Iterable<JsonTokenEvent> tokens;

  public JsonObjectEvent(JsonPath path, Collection<JsonTokenEvent> tokens) {
    this.path = path;
    this.tokens = tokens;
  }

  public JsonPath getPath() {
    return path;
  }

  public Iterable<JsonTokenEvent> getTokens() {
    return tokens;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JsonObjectEvent that = (JsonObjectEvent) o;
    return Objects.equals(path, that.path) &&
      Objects.equals(tokens, that.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, tokens);
  }

  @Override
  public String toString() {
    return "JsonObjectEvent{path=" + path + ", tokens=" + tokens + '}';
  }
}
