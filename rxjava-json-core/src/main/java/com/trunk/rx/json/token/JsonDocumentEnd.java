package com.trunk.rx.json.token;

import java.util.Objects;

public class JsonDocumentEnd implements JsonToken {

  public static final JsonDocumentEnd INSTANCE = new JsonDocumentEnd();

  private JsonDocumentEnd() {
    // do nothing
  }

  @Override
  public String value() {
    return "";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("JsonDocumentEnd{");
    sb.append('}');
    return sb.toString();
  }
}
