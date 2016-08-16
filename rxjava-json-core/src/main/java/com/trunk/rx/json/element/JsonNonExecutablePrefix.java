package com.trunk.rx.json.element;

public class JsonNonExecutablePrefix extends JsonRaw {
  private static final JsonNonExecutablePrefix INSTANCE = new JsonNonExecutablePrefix();

  public static final JsonNonExecutablePrefix instance() {
    return INSTANCE;
  }

  private JsonNonExecutablePrefix() {
    super(")]}'\n");
  }
}
