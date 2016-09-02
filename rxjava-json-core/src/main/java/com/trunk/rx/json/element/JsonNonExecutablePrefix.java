package com.trunk.rx.json.element;

public class JsonNonExecutablePrefix extends JsonRaw {
  private static final class Holder {
    private static final JsonNonExecutablePrefix INSTANCE = new JsonNonExecutablePrefix();
  }
  public static final String PREFIX = ")]}'\n";

  public static JsonNonExecutablePrefix instance() {
    return Holder.INSTANCE;
  }

  private JsonNonExecutablePrefix() {
    super(PREFIX);
  }
}
