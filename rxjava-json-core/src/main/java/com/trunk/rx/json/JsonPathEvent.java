package com.trunk.rx.json;

import java.util.Objects;

import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;

public class JsonPathEvent {
  private final JsonPath matchedPathFragment;
  private final Observable<JsonToken> tokens;

  public JsonPathEvent(JsonPath matchedPathFragment, Observable<JsonToken> tokens) {
    this.matchedPathFragment = matchedPathFragment;
    this.tokens = tokens;
  }

  public JsonPath getMatchedPathFragment() {
    return matchedPathFragment;
  }

  public Observable<JsonToken> getTokens() {
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
    JsonPathEvent that = (JsonPathEvent) o;
    return Objects.equals(matchedPathFragment, that.matchedPathFragment) &&
      Objects.equals(tokens, that.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchedPathFragment, tokens);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("JsonPathEvent{");
    sb.append("matchedPathFragment='").append(matchedPathFragment).append('\'');
    sb.append(", tokens=").append(tokens);
    sb.append('}');
    return sb.toString();
  }
}
