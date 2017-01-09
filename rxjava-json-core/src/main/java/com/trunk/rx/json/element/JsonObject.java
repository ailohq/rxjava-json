package com.trunk.rx.json.element;

import com.trunk.rx.json.RxJson;
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
  private final boolean suppressNulls;

  public static <T extends JsonElement> JsonObject<T> of() {
    return new JsonObject<>(Observable.empty(), false);
  }

  public static <T extends JsonElement> JsonObject<T> of(Observable<Entry<T>> elements) {
    return new JsonObject<>(elements, false);
  }

  public static <T extends JsonElement> JsonObject<T> of(Iterable<Entry<T>> elements) {
    return new JsonObject<>(Observable.from(elements), false);
  }

  @SafeVarargs
  public static <T extends JsonElement> JsonObject<T> of(Entry<T>... elements) {
    return new JsonObject<>(Observable.from(elements), false);
  }

  public static <T extends JsonElement> Entry<T> entry(String key, T value) {
    return entry(key, just(value));
  }

  public static <T extends JsonElement> Entry<T> entry(String key, Observable<T> value) {
    return new Entry<>(key, value);
  }

  protected JsonObject(Observable<Entry<T>> elements, boolean suppressNulls) {
    super(
        Observable.<JsonToken>just(JsonObjectStart.instance())
            .concatWith(
                elements
                    .concatMap(
                        entry ->
                          entry.getValue().take(1)
                            .flatMap(
                              value ->
                                getKeyValuePairTokens(entry.key, value)
                            )
                            .switchIfEmpty(
                              suppressNulls ?
                                Observable.empty() :
                                getKeyValuePairTokens(entry.key, RxJson.valueBuilder().Null())
                            )
                    )
                    .skip(1) // the first comma
            )
            .concatWith(Observable.just(JsonObjectEnd.instance()))
    );
    this.elements = elements;
    this.suppressNulls = suppressNulls;
  }

  private static Observable<JsonToken> getKeyValuePairTokens(String key, JsonElement value) {
    return Observable.<JsonToken>just(JsonComma.instance())
        .concatWith(Observable.just(JsonQuote.instance(), JsonName.of(key), JsonQuote.instance()))
        .concatWith(Observable.just(JsonColon.instance()))
        .concatWith(value);
  }

  public JsonObject<T> addAll(Observable<Entry<T>> elements) {
    return new JsonObject<>(this.elements.concatWith(elements), suppressNulls);
  }

  public JsonObject<T> addAll(Iterable<Entry<T>> elements) {
    return new JsonObject<>(this.elements.concatWith(Observable.from(elements)), suppressNulls);
  }

  public JsonObject<T> add(String key, T value) {
    return new JsonObject<>(this.elements.concatWith(Observable.just(entry(key, value))), suppressNulls);
  }

  public JsonObject<T> add(String key, Observable<T> value) {
    return new JsonObject<>(this.elements.concatWith(Observable.just(entry(key, value))), suppressNulls);
  }

  public JsonObject<T> suppressNulls() {
    return new JsonObject<>(this.elements, true);
  }

  public JsonObject<T> suppressNulls(boolean suppress) {
    return new JsonObject<>(this.elements, suppress);
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
