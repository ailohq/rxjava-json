package com.trunk.rx.json.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rx.Observable;

public abstract class JsonPath implements Cloneable {

  private static final JsonPathParser parser = new JsonPathParser();

  final Optional<JsonPath> nextPathToken;

  private Integer length;
  private String string;

  public static JsonPath from(List<JsonPath> pathTokens) {
    if (pathTokens.isEmpty()) {
      throw new IllegalArgumentException("Cannot have an empty JsonPath");
    }
    JsonPath root = null;
    for (int i = pathTokens.size() - 1; i >= 0; --i) {
      root = pathTokens.get(i).cloneWith(root);
    }
    return root;
  }

  public static JsonPath from(JsonPath... pathTokens) {
    if (pathTokens.length == 0) {
      throw new IllegalArgumentException("Cannot have an empty JsonPath");
    }
    JsonPath root = null;
    for (int i = pathTokens.length - 1; i >= 0; --i) {
      root = pathTokens[i].cloneWith(root);
    }
    return root;
  }

  public static JsonPath parse(String path) {
    return from(parser.parse(path));
  }

  JsonPath(JsonPath nextPathToken) {
    this.nextPathToken = Optional.ofNullable(nextPathToken);
  }

  public Observable<JsonPath> match(JsonPath pathToTest) {
    return doMatch(Optional.of(pathToTest), Collections.emptyList())
      .map(m -> Observable.just(m))
      .orElse(Observable.empty())
      .map(l -> from(l));
  }

  @Override
  public String toString() {
    // memoise string
    return string != null ? string : (string = fragment() + nextPathToken.map(f -> f.toString()).orElse(""));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JsonPath jsonPath = (JsonPath) o;
    return tokenEquals(jsonPath) && Objects.equals(nextPathToken, jsonPath.nextPathToken);
  }

  @Override
  public int hashCode() {
    return tokenHash() + 37 * nextPathToken.hashCode();
  }

  public int length() {
    // memoise length
    return length != null ? length : (length = 1 + nextPathToken.map(t -> t.length()).orElse(0));
  }

  abstract int tokenHash();

  abstract Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments);

  abstract String fragment();

  abstract JsonPath cloneWith(JsonPath nextPathToken);

  abstract AccessorType accessorType();

  abstract boolean tokenEquals(JsonPath jsonPath);

  Optional<List<JsonPath>> matchNextFragment(JsonPath pathToTest, List<JsonPath> matchedFragments) {
    List<JsonPath> newMatchedFragments = new ArrayList<>(matchedFragments);
    newMatchedFragments.add(pathToTest);
    return nextPathToken
      .map(myNext -> myNext.doMatch(pathToTest.nextPathToken, newMatchedFragments))
      .orElse(Optional.of(newMatchedFragments));
  }
}
