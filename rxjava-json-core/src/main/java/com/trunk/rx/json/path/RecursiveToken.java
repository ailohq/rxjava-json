package com.trunk.rx.json.path;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecursiveToken extends JsonPath {

  private static final class Holder {
    private static RecursiveToken INSTANCE = new RecursiveToken();
  }

  public static RecursiveToken instance() {
    return Holder.INSTANCE;
  }

  private RecursiveToken() {
    this(null);
  }

  private RecursiveToken(JsonPath nextPathToken) {
    super(nextPathToken);
  }

  @Override
  int tokenHash() {
    return Objects.hashCode("<recursive>");
  }

  @Override
  Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    if (!nextPathToken.isPresent()) {
      return Optional.of(matchedFragments);
    }

    Optional<JsonPath> tempPath = pathToTest;
    // start with skipping 0 elements
    while (tempPath.isPresent()) {
      Optional<JsonPath> finalTempPath = tempPath;
      Optional<List<JsonPath>> result = nextPathToken.flatMap(next -> next.doMatch(finalTempPath, matchedFragments));
      if (result.isPresent()) {
        return result;
      }
      tempPath.ifPresent(p -> matchedFragments.add(p));
      tempPath = tempPath.flatMap(p -> p.nextPathToken);
    }

    return Optional.empty();
  }

  @Override
  public String fragment() {
    return nextPathToken
      .map(f -> f.accessorType() == AccessorType.OBJECT ? "." : "..")
      .orElse("..*");
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new RecursiveToken(nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return AccessorType.OBJECT;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof RecursiveToken;
  }
}
