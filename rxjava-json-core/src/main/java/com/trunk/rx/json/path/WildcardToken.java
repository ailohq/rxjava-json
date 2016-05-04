package com.trunk.rx.json.path;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WildcardToken extends JsonPath {

  private final String fragment;
  private final AccessorType accessorType;

  public static WildcardToken object() {
    return new WildcardToken(".*", AccessorType.OBJECT);
  };

  public static WildcardToken array() {
    return new WildcardToken("[*]", AccessorType.ARRAY);
  };

  private WildcardToken(String fragment, AccessorType accessorType) {
    this(fragment, null, accessorType);
  }

  private WildcardToken(String fragment, JsonPath nextPathToken, AccessorType accessorType) {
    super(nextPathToken);
    this.fragment = fragment;
    this.accessorType = accessorType;
  }

  @Override
  int tokenHash() {
    return Objects.hashCode(fragment);
  }

  @Override
  Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return pathToTest
      .flatMap(p -> matchNextFragment(p, matchedFragments)); // skip this fragment
  }

  @Override
  public String fragment() {
    return fragment;
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new WildcardToken(fragment, nextPathToken, accessorType);
  }

  @Override
  AccessorType accessorType() {
    return accessorType;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof WildcardToken;
  }
}
