package com.trunk.rx.json.token;

public class JsonDocumentEnd extends BaseToken {

  public static final JsonDocumentEnd INSTANCE = new JsonDocumentEnd();

  private JsonDocumentEnd() {
    // do nothing
  }

  @Override
  public boolean isDocumentEnd() {
    return true;
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
