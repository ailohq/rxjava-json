package com.trunk.rx.json;

import java.util.Objects;

import com.trunk.rx.json.path.JsonPath;

public class JsonPathEvent {
  private final JsonPath matchedPathFragment;
  private final JsonTokenEvent token;

  public JsonPathEvent(JsonPath matchedPathFragment, JsonTokenEvent token) {
    this.matchedPathFragment = matchedPathFragment;
    this.token = token;
  }

  public JsonPath getMatchedPathFragment() {
    return matchedPathFragment;
  }

  public JsonTokenEvent getTokenEvent() {
    return token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JsonPathEvent that = (JsonPathEvent) o;
    return Objects.equals(matchedPathFragment, that.matchedPathFragment) &&
      Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchedPathFragment, token);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("JsonPathEvent{");
    sb.append("matchedPathFragment='").append(matchedPathFragment).append('\'');
    sb.append(", token=").append(token);
    sb.append('}');
    return sb.toString();
  }
}
