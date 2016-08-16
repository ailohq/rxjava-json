package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonElement extends Observable<JsonToken> {

  protected JsonElement(Observable<JsonToken> tokens) {
    super(subscriber -> tokens.subscribe(subscriber));
  }

  protected JsonElement(JsonToken token) {
    this(Observable.just(token));
  }

  public Observable<JsonToken> withNonExecutionPrefix() {
    return JsonNonExecutablePrefix.instance().concatWith(this);
  }
}
