package com.trunk.rx.json.token;

public class JsonArray {
  public static JsonArrayStart start() {
    return JsonArrayStart.instance();
  }
  public static JsonArrayEnd end() {
    return JsonArrayEnd.instance();
  }
}
