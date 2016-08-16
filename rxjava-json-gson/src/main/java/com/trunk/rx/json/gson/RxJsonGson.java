package com.trunk.rx.json.gson;

import com.trunk.rx.json.gson.transformer.TransformerRxJsonGson;
import com.trunk.rx.json.path.JsonPath;

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

}
