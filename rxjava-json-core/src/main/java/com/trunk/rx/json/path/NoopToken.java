package com.trunk.rx.json.path;

import java.util.List;
import java.util.Optional;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NoopToken extends JsonPath {

  public static final JsonPath INSTANCE = new NoopToken();

  private NoopToken() {
    super(null);
  }

  @Override
  public int length() {
    return Integer.MAX_VALUE;
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
    throw new NotImplementedException();
  }

  @Override
  AccessorType accessorType() {
    return null;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return false;
  }
}
