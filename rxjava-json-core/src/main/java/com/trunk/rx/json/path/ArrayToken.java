package com.trunk.rx.json.path;

import java.util.List;
import java.util.Optional;

public abstract class ArrayToken extends JsonPath {
  ArrayToken(JsonPath nextPathToken) {
    super(nextPathToken);
  }

  public static ArrayToken of(List<Integer> integerElements) {
    if (integerElements.isEmpty() || integerElements.size() > 3) {
      throw new IllegalArgumentException();
    }
    if (integerElements.size() == 1) {
      return ArrayIndexToken.of(integerElements.get(0));
    }
    if (integerElements.size() == 2) {
      return ArraySliceToken.of(integerElements.get(0),integerElements.get(1));
    }
    return ArrayStepToken.of(integerElements.get(0), integerElements.get(1), integerElements.get(2));
  }

  @Override
  final String fragment() {
    return "[" + arrayValue() + "]";
  }

  @Override
  final Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return pathToTest
      .filter(
        p -> tokenEquals(p) || doesMatch(p)
      )
      .flatMap(p -> matchNextFragment(p, matchedFragments));
  }

  abstract boolean doesMatch(JsonPath pathToTest);

  abstract String arrayValue();
}
