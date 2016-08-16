package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonColon;
import com.trunk.rx.json.token.JsonComma;
import com.trunk.rx.json.token.JsonName;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;

public class JsonObject extends JsonElement {

  private final Observable<Entry> elements;

  public static JsonObject of() {
    return new JsonObject(Observable.empty());
  }

  public static JsonObject of(Observable<Entry> elements) {
    return new JsonObject(elements);
  }

  public static JsonObject of(Iterable<Entry> elements) {
    return new JsonObject(Observable.from(elements));
  }

  protected JsonObject(Observable<Entry> elements) {
    super(
        Observable.<JsonToken>just(com.trunk.rx.json.token.JsonObject.start())
            .concatWith(
                elements
                    .concatMap(
                        entry ->
                            Observable.<JsonToken>just(JsonComma.instance())
                                .concatWith(Observable.just(JsonName.of(entry.getKey())))
                                .concatWith(Observable.just(JsonColon.instance()))
                                .concatWith(entry.getValue())
                    )
                    .skip(1)
            )
            .concatWith(Observable.just(com.trunk.rx.json.token.JsonObject.end()))
    );
    this.elements = elements;
  }

  public JsonObject addAll(Observable<? extends Entry> elements) {
    return new JsonObject(this.elements.concatWith(elements));
  }

  public JsonObject addAll(Iterable<? extends Entry> elements) {
    return new JsonObject(this.elements.concatWith(Observable.from(elements)));
  }

  public JsonObject add(String key, JsonElement value) {
    return new JsonObject(this.elements.concatWith(Observable.just(new Entry(key, value))));
  }

  public static class Entry {

    private String key;
    private JsonElement value;

    public Entry(String key, JsonElement value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public JsonElement getValue() {
      return value;
    }
  }
}
