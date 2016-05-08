package com.trunk.rx.json.gson.transformer;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.gson.operator.OperatorJsonGson;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.transformer.TransformerRxJson;

import rx.Observable;
import rx.functions.Func3;

public class TransformerRxJsonGson<T> implements Observable.Transformer<String, T> {
  private static final Gson GSON = new Gson();
  private static final Func3<JsonPath, JsonElement, Gson, Object> OBJECT_CONVERTER = (path, element, gson) -> gson.fromJson(element, Object.class);

  private final TransformerRxJson transformerRxJson;
  private final OperatorJsonGson operatorJsonGson;
  private final Gson gson;
  private final Func3<JsonPath, JsonElement, Gson, T> converter;

  public TransformerRxJsonGson<T> lenient() {
    return new TransformerRxJsonGson<>(transformerRxJson.lenient(), operatorJsonGson, gson, converter);
  }

  public TransformerRxJsonGson<T> strict() {
    return new TransformerRxJsonGson<>(transformerRxJson.strict(), operatorJsonGson, gson, converter);
  }

  public TransformerRxJsonGson<T> using (Gson gson) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, converter);
  }

  public <R> TransformerRxJsonGson<R> using(Func3<JsonPath, JsonElement, Gson, R> converter) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, converter);
  }

  public <R> TransformerRxJsonGson<R> to(Type type) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, (path, element, gson) -> gson.fromJson(element, type));
  }

  public <R> TransformerRxJsonGson<R> to(Class<R> c) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, (path, element, gson) -> gson.fromJson(element, c));
  }

  @Override
  public Observable<T> call(Observable<String> upstream) {
    return upstream
      .compose(transformerRxJson)
      .lift(operatorJsonGson)
      .map(event -> converter.call(event.getPath(), event.getElement(), gson));
  }

  private TransformerRxJsonGson(TransformerRxJson transformerRxJson,
                                OperatorJsonGson operatorJsonGson,
                                Gson gson,
                                Func3<JsonPath, JsonElement, Gson, T> converter) {
    this.transformerRxJson = transformerRxJson;
    this.operatorJsonGson = operatorJsonGson;
    this.gson = gson;
    this.converter = converter;
  }

  public static TransformerRxJsonGson<Object> from(String... paths) {
    return new TransformerRxJsonGson<>(RxJson.from(paths),
                                       new OperatorJsonGson(),
                                       GSON,
                                       OBJECT_CONVERTER);
  }

  public static TransformerRxJsonGson<Object> from(JsonPath... paths) {
    return new TransformerRxJsonGson<>(RxJson.from(paths),
                                       new OperatorJsonGson(),
                                       GSON,
                                       OBJECT_CONVERTER);
  }
}
