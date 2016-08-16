package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonComma;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonArray extends JsonElement {

  private final Observable<JsonElement> elements;

  public static JsonArray of() {
    return new JsonArray(Observable.empty());
  }

  public static JsonArray of(Observable<JsonElement> elements) {
    return new JsonArray(elements);
  }

  public static JsonArray of(Iterable<JsonElement> elements) {
    return new JsonArray(Observable.from(elements));
  }

  public static JsonArray of(JsonElement... elements) {
    return new JsonArray(Observable.from(elements));
  }

  protected JsonArray(Observable<JsonElement> elements) {
    super(
      Observable.<JsonToken>just(com.trunk.rx.json.token.JsonArray.start())
        .concatWith(
          elements
            .concatMap(jsonElement -> Observable.<JsonToken>just(JsonComma.instance()).concatWith(jsonElement))
            .skip(1)
        )
        .concatWith(Observable.just(com.trunk.rx.json.token.JsonArray.end()))
    );
    this.elements = elements;
  }

  public JsonArray addAll(Observable<? extends JsonElement> elements) {
    return new JsonArray(this.elements.concatWith(elements));
  }

  public JsonArray addAll(Iterable<? extends JsonElement> elements) {
    return new JsonArray(this.elements.concatWith(Observable.from(elements)));
  }

  public JsonArray add(JsonElement element) {
    return new JsonArray(this.elements.concatWith(Observable.just(element)));
  }
}
