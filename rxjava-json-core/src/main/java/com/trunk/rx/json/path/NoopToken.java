package com.trunk.rx.json.path;

import java.util.List;
import java.util.Optional;

public class NoopToken extends JsonPath {

  private static final class Holder {
    private static final JsonPath INSTANCE = new NoopToken();
  }

  public static JsonPath instance() {
    return Holder.INSTANCE;
  }

  private NoopToken() {
    super(null);
  }

  @Override
  public int length() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  int tokenHash() {
    return 0;
  }

  @Override
  Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return Optional.empty();
  }

  @Override
  String fragment() {
    return "<NOOP>";
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return this;
  }

  @Override
  AccessorType accessorType() {
    return null;
  }

  @Override
  public boolean tokenEquals(JsonPath jsonPath) {
    return false;
  }
}
