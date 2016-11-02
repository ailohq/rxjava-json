package com.trunk.rx.json.path;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArrayUnionToken extends ArrayToken {

  private final Collection<ArrayToken> delegates;

  public static ArrayToken using(ArrayToken... delegates) {
    return using(Arrays.asList(delegates));
  }

  public static ArrayToken using(List<ArrayToken> delegates) {
    if (delegates.isEmpty()) {
      throw new IllegalArgumentException();
    }
    if (delegates.size() == 1) {
      return delegates.get(0);
    }
    return new ArrayUnionToken(delegates, null);
  }

  ArrayUnionToken(Collection<ArrayToken> delegates, JsonPath nextPathToken) {
    super(nextPathToken);
    this.delegates = delegates;
    if (delegates.isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  boolean doesMatch(JsonPath pathToTest) {
    return pathToTest instanceof ArrayIndexToken && delegates.stream().anyMatch(d -> d.tokenEquals(pathToTest));
  }

  @Override
  String arrayValue() {
    return String.join(",", delegates.stream().map(ArrayToken::arrayValue).collect(Collectors.toList()));
  }

  @Override
  int tokenHash() {
    return Objects.hashCode(delegates);
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new ArrayUnionToken(delegates, nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.ARRAY;
  }

  @Override
  public boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof ArrayUnionToken && Objects.equals(delegates, ((ArrayUnionToken) jsonPath).delegates);
  }
}
