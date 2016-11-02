package com.trunk.rx.json;

import com.trunk.rx.json.element.JsonArray;
import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.element.JsonObject;
import com.trunk.rx.json.element.JsonRaw;
import com.trunk.rx.json.element.JsonValueBuilder;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonToken;
import com.trunk.rx.json.transformer.TransformerJsonTokenToString;
import com.trunk.rx.json.transformer.TransformerRxJson;
import rx.Observable;

/**
 * The entry point to parsing and writing JSON using RxJava. This aggregates the core functionality of
 * RxJson.
 */
public class RxJson {

  /**
   * A JSON token parser that will emit tokens for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>.
   *
   * @param paths the JSON Paths to match as Strings
   * @return an immutable Transformer to parse an Observable of Strings and emit JsonPathEvents matching the given paths
   */
  public static TransformerRxJson parse(String... paths) {
    return TransformerRxJson.from(paths);
  }

  /**
   * A JSON token parser that will emit tokens for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>.
   *
   * @param paths the JsonPaths to match
   * @return an immutable Transformer to parse an Observable of Strings and emit JsonPathEvents matching the given paths
   */
  public static TransformerRxJson parse(JsonPath... paths) {
    return TransformerRxJson.from(paths);
  }

  /**
   * @return a new empty, immutable JsonArray
   */
  public static <T extends JsonElement> JsonArray<T> newArray() {
    return JsonArray.of();
  }

  /**
   * @return a new immutable JsonArray with the given elements
   */
  public static <T extends JsonElement> JsonArray<T> newArray(Observable<T> values) {
    return JsonArray.of(values);
  }

  /**
   * @return a new immutable JsonArray with the given elements
   */
  @SafeVarargs
  public static <T extends JsonElement> JsonArray<T> newArray(T... values) {
    return JsonArray.of(values);
  }

  /**
   * @return a new immutable JsonArray with the given elements
   */
  public static <T extends JsonElement> JsonArray<T> newArray(Iterable<T> values) {
    return JsonArray.of(values);
  }

  /**
   * @return a new empty, immutable JsonObject
   */
  public static <T extends JsonElement> JsonObject<T> newObject() {
    return JsonObject.of();
  }

  /**
   * @return a new immutable JsonArray with the given entries
   */
  public static <T extends JsonElement> JsonObject<T> newObject(Observable<JsonObject.Entry<T>> values) {
    return JsonObject.of(values);
  }

  /**
   * @return a new immutable JsonArray with the given entries
   */
  @SafeVarargs
  public static <T extends JsonElement> JsonObject<T> newObject(JsonObject.Entry<T>... values) {
    return JsonObject.of(values);
  }

  /**
   * @return a new immutable JsonArray with the given entries
   */
  public static <T extends JsonElement> JsonObject<T> newObject(Iterable<JsonObject.Entry<T>> values) {
    return JsonObject.of(values);
  }

  /**
   * @return a new immutable JsonEntry to insert into a JsonObject
   */
  public static <T extends JsonElement> JsonObject.Entry<T> newEntry(String key, T value) {
    return JsonObject.entry(key, value);
  }

  /**
   * @return a new immutable JsonRaw
   */
  public static JsonRaw newRaw(String value) {
    return JsonRaw.of(value);
  }

  /**
   * @return a new immutable JsonValueBuilder that can be used to produce JSON values
   */
  public static JsonValueBuilder valueBuilder() {
    return JsonValueBuilder.instance();
  }

  /**
   * @return a transformer to convert JsonTokens to JSON as an Observable of Strings
   */
  public static Observable.Transformer<JsonToken, String> toJson() {
    return TransformerJsonTokenToString.instance();
  }

  private RxJson() {
    // do nothing
  }
}
