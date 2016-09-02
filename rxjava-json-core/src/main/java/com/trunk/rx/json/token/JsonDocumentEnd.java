package com.trunk.rx.json.token;

public class JsonDocumentEnd extends BaseToken {

  private static final class Holder {
    private static final JsonDocumentEnd INSTANCE = new JsonDocumentEnd();
  }

  public static JsonDocumentEnd instance() {
    return Holder.INSTANCE;
  }

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
