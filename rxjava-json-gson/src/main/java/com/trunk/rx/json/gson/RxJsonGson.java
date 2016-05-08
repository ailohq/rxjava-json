package com.trunk.rx.json.gson;

import com.trunk.rx.json.gson.transformer.TransformerRxJsonGson;
import com.trunk.rx.json.path.JsonPath;

public class RxJsonGson {
  public static TransformerRxJsonGson<Object> from(String... paths) {
    return TransformerRxJsonGson.from(paths);
  }

  public static TransformerRxJsonGson<Object> from(JsonPath... paths) {
    return TransformerRxJsonGson.from(paths);
  }
}
