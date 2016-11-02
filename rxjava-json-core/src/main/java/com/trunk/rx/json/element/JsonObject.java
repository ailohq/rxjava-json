package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonColon;
import com.trunk.rx.json.token.JsonComma;
import com.trunk.rx.json.token.JsonName;
import com.trunk.rx.json.token.JsonObjectEnd;
import com.trunk.rx.json.token.JsonObjectStart;
import com.trunk.rx.json.token.JsonQuote;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonObject<T extends JsonElement> extends JsonElement {

  private final Observable<Entry<T>> elements;

  public static <T extends JsonElement> JsonObject<T> of() {
    return new JsonObject<>(Observable.empty());
  }

  public static <T extends JsonElement> JsonObject<T> of(Observable<Entry<T>> elements) {
    return new JsonObject<>(elements);
  }

  public static <T extends JsonElement> JsonObject<T> of(Iterable<Entry<T>> elements) {
    return new JsonObject<>(Observable.from(elements));
  }

  @SafeVarargs
  public static <T extends JsonElement> JsonObject<T> of(Entry<T>... elements) {
    return new JsonObject<>(Observable.from(elements));
  }

  public static <T extends JsonElement> Entry<T> entry(String key, T value) {
    return entry(key, just(value));
  }

  public static <T extends JsonElement> Entry<T> entry(String key, Observable<T> value) {
    return new Entry<>(key, value);
  }

  protected JsonObject(Observable<Entry<T>> elements) {
    super(
        Observable.<JsonToken>just(JsonObjectStart.instance())
            .concatWith(
                elements
                    .concatMap(
                        entry ->
                            Observable.<JsonToken>just(JsonComma.instance())
                                .concatWith(Observable.just(JsonQuote.instance(), JsonName.of(entry.getKey()), JsonQuote.instance()))
                                .concatWith(Observable.just(JsonColon.instance()))
                                .concatWith(
                                  entry.getValue()
                                    .take(1)
                                    .cast(JsonElement.class)
                                    .defaultIfEmpty(JsonValueBuilder.instance().Null())
                                    .flatMap(e -> e)
                                )
                    )
                    .skip(1)
            )
            .concatWith(Observable.just(JsonObjectEnd.instance()))
    );
    this.elements = elements;
  }

  public JsonObject<T> addAll(Observable<Entry<T>> elements) {
    return new JsonObject<>(this.elements.concatWith(elements));
  }

  public JsonObject<T> addAll(Iterable<Entry<T>> elements) {
    return new JsonObject<>(this.elements.concatWith(Observable.from(elements)));
  }

  public JsonObject<T> add(String key, T value) {
    return new JsonObject<>(this.elements.concatWith(Observable.just(entry(key, value))));
  }

  public JsonObject<T> add(String key, Observable<T> value) {
    return new JsonObject<>(this.elements.concatWith(Observable.just(entry(key, value))));
  }

  public static final class Entry<T extends JsonElement> {

    private String key;
    private Observable<T> value;

    public Entry(String key, Observable<T> value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public Observable<T> getValue() {
      return value;
    }
  }
}
