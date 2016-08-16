package com.trunk.rx.json.path;

import com.trunk.rx.json.exception.MalformedPathException;

import java.util.Objects;

public class ArrayStepToken extends ArrayToken {

  private final long start;
  private final long end;
  private final int step;

  public static ArrayStepToken of(Integer start, Integer end, int step) {
    return new ArrayStepToken((start != null ? start : 0), (end != null ? end : Long.MAX_VALUE), step);
  }

  private ArrayStepToken(long start, long end, int step) {
    this(start, end, step, null);
  }

  private ArrayStepToken(long start, long end, int step, JsonPath nextPathToken) {
    super(nextPathToken);
    if (start >= end) {
      throw new MalformedPathException("Start must be before end");
    }
    if (step < 1) {
      throw new MalformedPathException("Step must be a positive integer");
    }
    this.start = start;
    this.end = end;
    this.step = step;
  }

  @Override
  boolean doesMatch(JsonPath pathToTest) {
    if (!(pathToTest instanceof ArrayIndexToken)) {
      return false;
    }
    long l = (((ArrayIndexToken) pathToTest).index - start);
    return l >= 0 && l % step == 0 && l < (end - start);
  }

  @Override
  String arrayValue() {
    return (start != 0 ? start : "") + ":" + (end != Long.MAX_VALUE ? end : "") + ":" + step;
  }

  @Override
  int tokenHash() {
    return Objects.hash(start, end, step);
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new ArrayStepToken(start, end, step, nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.ARRAY;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof ArrayStepToken &&
      ((ArrayStepToken) jsonPath).start == start &&
      ((ArrayStepToken) jsonPath).end == end &&
      ((ArrayStepToken) jsonPath).step == step;
  }
}
