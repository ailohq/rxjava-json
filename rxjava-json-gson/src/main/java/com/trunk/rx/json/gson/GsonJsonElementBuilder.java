package com.trunk.rx.json.gson;

import com.google.gson.Gson;
import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.element.JsonRaw;
import com.trunk.rx.json.gson.transformer.TransformerRxJsonGson;

import java.lang.reflect.Type;

/**
 * A simple wrapper to produce RxJavaJson {@link JsonElement JsonElements} using <a href="https://github.com/google/gson">Gson</a>.
 */
public class GsonJsonElementBuilder {

  public static final class Holder {
    public static final GsonJsonElementBuilder DEFAULT = new GsonJsonElementBuilder();
  }

  private final Gson gson;

  public GsonJsonElementBuilder() {
    this(TransformerRxJsonGson.Holder.DEFAULT_GSON);
  }

  public GsonJsonElementBuilder(Gson gson) {
    this.gson = gson;
  }

  public JsonElement toJson(Object src) {
    return JsonRaw.of(gson.toJson(src));
  }

  public JsonElement toJson(Object src, Type typeOfSrc) {
    return JsonRaw.of(gson.toJson(src, typeOfSrc));
  }

  public JsonElement toJson(com.google.gson.JsonElement src) {
    return JsonRaw.of(gson.toJson(src));
  }
}
