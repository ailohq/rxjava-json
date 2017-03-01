package com.trunk.rx.json.element;

import rx.Observable;

public class JsonNull extends JsonElement {

  private static final class Holder {
    private static final JsonNull INSTANCE = new JsonNull();
  }

  public static JsonNull instance() {
    return Holder.INSTANCE;
  }

  public JsonNull() {
    super(Observable.just(com.trunk.rx.json.token.JsonNull.instance()));
  }
}
