package com.trunk.rx.json.gson;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.trunk.rx.json.path.JsonPath;

public class GsonPathEvent {
  private final JsonPath path;
  private final JsonElement element;

  public GsonPathEvent(JsonPath path, JsonElement element) {
    this.path = path;
    this.element = element;
  }

  public JsonElement getElement() {
    return element;
  }

  public JsonPath getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GsonPathEvent that = (GsonPathEvent) o;
    return Objects.equals(path, that.path) &&
      Objects.equals(element, that.element);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, element);
  }

  @Override
  public String toString() {
    return "GsonPathEvent{element=" + element + ", path=" + path + '}';
  }
}
