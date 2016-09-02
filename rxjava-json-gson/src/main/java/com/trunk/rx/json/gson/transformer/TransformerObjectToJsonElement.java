package com.trunk.rx.json.gson.transformer;

import com.google.gson.Gson;
import com.trunk.rx.json.element.JsonElement;
import com.trunk.rx.json.gson.GsonJsonElementBuilder;
import rx.Observable;

import java.lang.reflect.Type;

/**
 * An immutable transformer to produce JsonElements from objects
 */
public class TransformerObjectToJsonElement implements Observable.Transformer<Object, JsonElement> {
  private static final class Holder {
    private static final TransformerObjectToJsonElement INSTANCE = new TransformerObjectToJsonElement(GsonJsonElementBuilder.Holder.DEFAULT, null);
  }

  private final GsonJsonElementBuilder gsonJsonElementBuilder;
  private final Type type;

  public static TransformerObjectToJsonElement instance() {
    return Holder.INSTANCE;
  }

  private TransformerObjectToJsonElement(GsonJsonElementBuilder gsonJsonElementBuilder, Type type) {
    this.gsonJsonElementBuilder = gsonJsonElementBuilder;
    this.type = type;
  }

  /**
   * @see Gson#toJson(Object, Type)
   */
  public TransformerObjectToJsonElement coerceFrom(Type type) {
    return new TransformerObjectToJsonElement(gsonJsonElementBuilder, type);
  }

  public TransformerObjectToJsonElement using(Gson gson) {
    return new TransformerObjectToJsonElement(new GsonJsonElementBuilder(gson), type);
  }

  public TransformerObjectToJsonElement using(GsonJsonElementBuilder gsonJsonElementBuilder) {
    return new TransformerObjectToJsonElement(gsonJsonElementBuilder, type);
  }

  @Override
  public Observable<JsonElement> call(Observable<Object> observable) {
    return observable.map(o -> type == null ? gsonJsonElementBuilder.toJson(o) : gsonJsonElementBuilder.toJson(o, type));
  }
}
