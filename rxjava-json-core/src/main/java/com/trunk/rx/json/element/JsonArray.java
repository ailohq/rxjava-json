package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonArrayEnd;
import com.trunk.rx.json.token.JsonArrayStart;
import com.trunk.rx.json.token.JsonComma;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonArray<T extends JsonElement> extends JsonElement {

  private final Observable<T> elements;

  public static <T extends JsonElement> JsonArray<T> of() {
    return new JsonArray<>(Observable.empty());
  }

  public static <T extends JsonElement> JsonArray<T> of(Observable<T> elements) {
    return new JsonArray<>(elements);
  }

  public static <T extends JsonElement> JsonArray<T> of(Iterable<T> elements) {
    return new JsonArray<>(Observable.from(elements));
  }

  @SafeVarargs
  public static <T extends JsonElement> JsonArray<T> of(T... elements) {
    return new JsonArray<>(Observable.from(elements));
  }

  protected JsonArray(Observable<T> elements) {
    super(
      Observable.<JsonToken>just(JsonArrayStart.instance())
        .concatWith(
          elements
            .concatMap(jsonElement -> Observable.<JsonToken>just(JsonComma.instance()).concatWith(jsonElement))
            .skip(1)
        )
        .concatWith(Observable.just(JsonArrayEnd.instance()))
    );
    this.elements = elements;
  }

  public JsonArray<T> addAll(Observable<? extends T> elements) {
    return new JsonArray<>(this.elements.concatWith(elements));
  }

  public JsonArray<T> addAll(Iterable<? extends T> elements) {
    return new JsonArray<>(this.elements.concatWith(Observable.from(elements)));
  }

  public JsonArray<T> add(T element) {
    return new JsonArray<>(this.elements.concatWith(Observable.just(element)));
  }
}
