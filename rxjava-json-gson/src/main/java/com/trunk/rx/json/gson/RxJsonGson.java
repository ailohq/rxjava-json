package com.trunk.rx.json.gson;

import com.google.gson.Gson;
import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.gson.transformer.TransformerObjectToJsonElement;
import com.trunk.rx.json.gson.transformer.TransformerRxJsonGson;
import com.trunk.rx.json.path.JsonPath;
import rx.Observable;

/**
 * An adpater for {@link com.trunk.rx.json.RxJson} to return Java objects from  a JSON stream using <a href="https://github.com/google/gson">Gson</a>.
 */
public class RxJsonGson {

  /**
   * A JSON token parser that will emit objects for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>.
   *
   * @param paths the JSON Paths to match as Strings
   * @return an immutable Transformer to parse an Observable of Strings and emit objects matching the given paths
   */
  public static TransformerRxJsonGson<Object> from(String... paths) {
    return TransformerRxJsonGson.from(paths);
  }

  /**
   * A JSON token parser that will emit objects for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>.
   *
   * @param paths the JSON Paths to match
   * @return an immutable Transformer to parse an Observable of Strings and emit objects matching the given paths
   */
  public static TransformerRxJsonGson<Object> from(JsonPath... paths) {
    return TransformerRxJsonGson.from(paths);
  }

  /**
   * Transform an Observable of objects to an observable of JsonElements
   */
  public static TransformerObjectToJsonElement toJsonElements() {
    return TransformerObjectToJsonElement.instance();
  }

  /**
   * @return the default GsonJsonElementBuilder
   */
  public static GsonJsonElementBuilder elementBuilder() {
    return GsonJsonElementBuilder.Holder.DEFAULT;
  }

  /**
   * @return a GsonJsonElementBuilder using the given Gson
   */
  public static GsonJsonElementBuilder elementBuilder(Gson gson) {
    return new GsonJsonElementBuilder(gson);
  }
}
