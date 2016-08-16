package com.trunk.rx.json.element;

import rx.Observable;

public class JsonRaw extends JsonElement {

  public static JsonRaw of(String rawString) {
    return new JsonRaw(rawString);
  }

  protected JsonRaw(String rawString) {
    super(Observable.just(com.trunk.rx.json.token.JsonRaw.of(rawString)));
  }
}
