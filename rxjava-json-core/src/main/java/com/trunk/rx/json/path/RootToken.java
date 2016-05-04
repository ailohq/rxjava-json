package com.trunk.rx.json.path;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RootToken extends JsonPath {

  public static RootToken INSTANCE = new RootToken();

  private RootToken() {
    this(null);
  }

  private RootToken(JsonPath nextPathToken) {
    super(nextPathToken);
  }

  @Override
  int tokenHash() {
    return Objects.hashCode("<root>");
  }

  @Override
  public Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return pathToTest
      .filter(p -> tokenEquals(p))
      .flatMap(p -> matchNextFragment(p, matchedFragments));
  }

  @Override
  String fragment() {
    return "$";
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new RootToken(nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.ROOT;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof RootToken;
  }
}
