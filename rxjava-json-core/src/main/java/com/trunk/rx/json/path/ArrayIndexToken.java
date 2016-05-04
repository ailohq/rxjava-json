package com.trunk.rx.json.path;

import java.util.Objects;

public class ArrayIndexToken extends ArrayToken {

  final int index;

  public static ArrayIndexToken of(int index) {
    return new ArrayIndexToken(index, null);
  }

  private ArrayIndexToken(int index, JsonPath nextPathToken) {
    super(nextPathToken);
    this.index = index;
  }

  public JsonPath increment() {
    return of(index + 1);
  }

  @Override
  boolean doesMatch(JsonPath pathToTest) {
    return false;
  }

  @Override
  String arrayValue() {
    return Integer.toString(index);
  }

  @Override
  int tokenHash() {
    return Objects.hashCode(index);
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new ArrayIndexToken(index, nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.ARRAY;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof ArrayIndexToken && ((ArrayIndexToken) jsonPath).index == index;
  }
}
