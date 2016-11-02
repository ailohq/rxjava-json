package com.trunk.rx.json.path;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RootToken extends JsonPath {

  private static RootToken INSTANCE = new RootToken();

  public static RootToken instance() {
    return INSTANCE;
  }

  private RootToken() {
    this(null);
  }

  private RootToken(JsonPath nextPathToken) {
    super(nextPathToken);
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  int tokenHash() {
    return Objects.hashCode("<root>");
  }

  @Override
  public Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return pathToTest
      .filter(this::tokenEquals)
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
  public boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof RootToken;
  }
}
