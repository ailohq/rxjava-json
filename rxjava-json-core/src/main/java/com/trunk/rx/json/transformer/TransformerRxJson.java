package com.trunk.rx.json.transformer;

import com.trunk.rx.character.CharacterObservable;
import com.trunk.rx.json.JsonObjectEvent;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.operator.OperatorJsonToken;
import com.trunk.rx.json.path.JsonPath;
import rx.Observable;

/**
 * A JSON token parser that will emit tokens for the given <a href="http://goessner.net/articles/JsonPath/">JSON paths</a>,
 * based on the <a href="https://github.com/google/gson">Gson</a> stream parser. This will not unmarshall JSON to
 * Java objects. RxJsonGson provides decoration to unmarshall objects using Gson.
 * <p>
 * The basic mode of operation is to emit a stream of token/path/matched path triples that can be assembled into objects,
 * where the path is the path to the token and the matched path is on of the paths given during creation.
 * {@link #collectObjects()} can be used to aggregate these triples by matched path so they can be further parsed into
 * Java objects.
 */
public class TransformerRxJson implements Observable.Transformer<String, JsonPathEvent> {
  private final OperatorJsonToken operatorJsonToken;
  private final TransformerJsonPath transformerJsonPath;

  public static TransformerRxJson from(String... paths) {
    return new TransformerRxJson(new OperatorJsonToken(), TransformerJsonPath.from(paths));
  }

  public static TransformerRxJson from(JsonPath... paths) {
    return new TransformerRxJson(new OperatorJsonToken(), TransformerJsonPath.from(paths));
  }

  public TransformerRxJson(OperatorJsonToken operatorJsonToken, TransformerJsonPath transformerJsonPath) {
    this.operatorJsonToken = operatorJsonToken;
    this.transformerJsonPath = transformerJsonPath;
  }

  /**
   * By default, RxJson is strict and only accepts JSON as specified by
   * <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>. This option makes the parser
   * liberal in what it accepts. This follows the rules from <a href="https://github.com/google/gson">Gson</a>.
   * In addition, running in lenient mode will parse multiple JSON documents in a single stream.
   *
   * @return a new TransformerRxJson that will parse leniently
   */
  public TransformerRxJson lenient() {
    return new TransformerRxJson(operatorJsonToken.lenient(), transformerJsonPath.lenient());
  }

  /**
   * @return a new TransformerRxJson that will parse strictly
   */
  public TransformerRxJson strict() {
    return new TransformerRxJson(operatorJsonToken.strict(), transformerJsonPath.strict());
  }

  /**
   * @return a new Transformer that will parse JSON and emit tokens aggregated by JsonPath
   */
  public Observable.Transformer<String, JsonObjectEvent> collectObjects() {
    return new TransformerCollectObjects(this);
  }

  @Override
  public Observable<JsonPathEvent> call(Observable<String> upstream) {
    return upstream.lift(CharacterObservable.toCharacter())
      .lift(operatorJsonToken)
      .compose(transformerJsonPath);
  }
}
