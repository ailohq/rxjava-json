package com.trunk.rx.json.token;

public class JsonArray {
  public static JsonArrayStart start() {
    return JsonArrayStart.INSTANCE;
  }
  public static JsonArrayEnd end() {
    return JsonArrayEnd.INSTANCE;
  }
}
