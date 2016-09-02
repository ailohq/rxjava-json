package com.trunk.rx.json.gson.transformer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.gson.operator.OperatorJsonGson;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.transformer.TransformerRxJson;
import rx.Observable;
import rx.functions.Func3;

import java.lang.reflect.Type;

/**
 * A JSON token parser that will emit objects for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>,
 * based on the <a href="https://github.com/google/gson">Gson</a> stream parser. This will not unmarshall JSON using Gson.
 */
public class TransformerRxJsonGson<T> implements Observable.Transformer<String, T> {
  public static final class Holder {
    public static final Gson DEFAULT_GSON = new Gson();
  }
  private static final Func3<JsonPath, JsonElement, Gson, Object> OBJECT_CONVERTER = (path, element, gson) -> gson.fromJson(element, Object.class);

  private final TransformerRxJson transformerRxJson;
  private final OperatorJsonGson operatorJsonGson;
  private final Gson gson;
  private final Func3<JsonPath, JsonElement, Gson, T> converter;

  /**
   * By default, RxJson is strict and only accepts JSON as specified by
   * <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>. This option makes the parser
   * liberal in what it accepts. This follows the rules from <a href="https://github.com/google/gson">Gson</a>.
   *
   * @return a new TransformerRxJsonGson that will parse leniently
   */
  public TransformerRxJsonGson<T> lenient() {
    return new TransformerRxJsonGson<>(transformerRxJson.lenient(), operatorJsonGson, gson, converter);
  }

  /**
   * @return a new TransformerRxJsonGson that will parse strictly
   */
  public TransformerRxJsonGson<T> strict() {
    return new TransformerRxJsonGson<>(transformerRxJson.strict(), operatorJsonGson, gson, converter);
  }

  /**
   * Replace the default
   *
   * @param gson the new Gson to use when creating objects
   * @return a new TransformerRxJsonGson
   */
  public TransformerRxJsonGson<T> using (Gson gson) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, converter);
  }

  /**
   * Use a custom function to parse each matched JSON object. This can be used to parse heterogeneous
   * data.
   *
   * @param converter a function to convert a JsonElement to an R given a JsonPath and a Gson
   * @param <R> the return type
   * @return a new TransformerRxJsonGson
   */
  public <R> TransformerRxJsonGson<R> using(Func3<JsonPath, JsonElement, Gson, R> converter) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, converter);
  }

  /**
   * Convert all matched paths to the given type.
   *
   * @return a new TransformerRxJsonGson
   */
  public <R> TransformerRxJsonGson<R> to(Type type) {
    return new TransformerRxJsonGson<>(transformerRxJson, operatorJsonGson, gson, (path, element, gson) -> gson.fromJson(element, type));
  }

  /**
   * Convert all matched paths to the given type.
   *
   * @return a new TransformerRxJsonGson
   */
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
    return new TransformerRxJsonGson<>(RxJson.parse(paths),
                                       new OperatorJsonGson(),
                                       Holder.DEFAULT_GSON,
                                       OBJECT_CONVERTER);
  }

  public static TransformerRxJsonGson<Object> from(JsonPath... paths) {
    return new TransformerRxJsonGson<>(RxJson.parse(paths),
                                       new OperatorJsonGson(),
                                       Holder.DEFAULT_GSON,
                                       OBJECT_CONVERTER);
  }
}
