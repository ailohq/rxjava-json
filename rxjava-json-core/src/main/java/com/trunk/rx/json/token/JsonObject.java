package com.trunk.rx.json.token;

public class JsonObject {
  public static JsonObjectStart start() {
    return JsonObjectStart.instance();
  }
  public static JsonObjectEnd end() {
    return JsonObjectEnd.instance();
  }
}
