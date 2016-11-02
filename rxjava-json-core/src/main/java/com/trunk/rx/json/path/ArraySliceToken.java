package com.trunk.rx.json.path;

import com.trunk.rx.json.exception.MalformedPathException;

import java.util.Objects;

public class ArraySliceToken extends ArrayToken {

  private final long start;
  private final long end;

  public static ArraySliceToken of(Integer start, Integer end) {
    return new ArraySliceToken(start, end);
  }

  private ArraySliceToken(Integer start, Integer end) {
    this(start != null ? start : 0, end != null ? end : Long.MAX_VALUE, null);
  }

  private ArraySliceToken(long start, long end, JsonPath nextPathToken) {
    super(nextPathToken);
    if (start >= end) {
      throw new MalformedPathException("Start must be before end");
    }
    this.start = start;
    this.end = end;
  }

  @Override
  boolean doesMatch(JsonPath pathToTest) {
    return
      pathToTest instanceof ArrayIndexToken &&
      ((ArrayIndexToken) pathToTest).index >= start &&
      ((ArrayIndexToken) pathToTest).index < end;
  }

  @Override
  String arrayValue() {
    return (start != 0 ? start : "") + ":" + (end != Long.MAX_VALUE ? end : "");
  }

  @Override
  int tokenHash() {
    return Objects.hash(start, end);
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new ArraySliceToken(start, end, nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.ARRAY;
  }

  @Override
  public boolean tokenEquals(JsonPath jsonPath) {
    return
      jsonPath instanceof ArraySliceToken &&
      ((ArraySliceToken) jsonPath).start == start &&
      ((ArraySliceToken) jsonPath).end == end;
  }
}
