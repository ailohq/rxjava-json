package com.trunk.rx.json;

import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.transformer.TransformerRxJson;

public class RxJson {
  public static TransformerRxJson from(String... paths) {
    return TransformerRxJson.from(paths);
  }
  public static TransformerRxJson from(JsonPath... paths) {
    return TransformerRxJson.from(paths);
  }
}
