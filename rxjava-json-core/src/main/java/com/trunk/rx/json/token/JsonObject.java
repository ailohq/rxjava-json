package com.trunk.rx.json.token;

public class JsonObject {
  public static JsonObjectStart start() {
    return JsonObjectStart.INSTANCE;
  }
  public static JsonObjectEnd end() {
    return JsonObjectEnd.INSTANCE;
  }
}
