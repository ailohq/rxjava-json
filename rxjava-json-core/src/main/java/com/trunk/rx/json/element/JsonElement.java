package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonElement extends Observable<JsonToken> {

  private static final class Holder {
    private static final JsonElement EMPTY = new JsonElement(Observable.empty());
  }

  public static JsonElement empty() {
    return Holder.EMPTY;
  }

  protected JsonElement(Observable<JsonToken> tokens) {
    super(tokens::subscribe);
  }

  protected JsonElement(JsonToken token) {
    this(Observable.just(token));
  }

  public Observable<JsonToken> withNonExecutionPrefix() {
    return JsonNonExecutablePrefix.instance().concatWith(this);
  }
}
