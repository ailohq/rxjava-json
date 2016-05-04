package com.trunk.rx.json.path;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class ObjectToken extends JsonPath {

  private static final Pattern validName = Pattern.compile("^[a-zA-Z_$][0-9a-zA-Z_$]*$");

  private final String name;
  private AccessorType accessorType;

  public static ObjectToken of(String name) {
    return new ObjectToken(name, null);
  }

  private ObjectToken(String name, JsonPath nextPathToken) {
    super(nextPathToken);
    this.name = name;
    accessorType = validName.matcher(name).matches() ? AccessorType.OBJECT : AccessorType.ARRAY;
  }

  @Override
  int tokenHash() {
    return Objects.hashCode(name);
  }

  @Override
  Optional<List<JsonPath>> doMatch(Optional<JsonPath> pathToTest, List<JsonPath> matchedFragments) {
    return pathToTest
      .filter(p -> tokenEquals(p))
      .flatMap(p -> matchNextFragment(p, matchedFragments));
  }

  @Override
  String fragment() {
    return accessorType == AccessorType.OBJECT ? "." + name : "[" + escape(name) + "]";
  }

  @Override
  JsonPath cloneWith(JsonPath nextPathToken) {
    return new ObjectToken(name, nextPathToken);
  }

  @Override
  AccessorType accessorType() {
    return accessorType;
  }

  @Override
  boolean tokenEquals(JsonPath jsonPath) {
    return jsonPath instanceof ObjectToken && Objects.equals(((ObjectToken) jsonPath).name, name);
  }

  private String escape(String name) {
    return name.replace("'", "\\'");
  }
}
