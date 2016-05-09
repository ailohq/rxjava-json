package com.trunk.rx.json.transformer;

import com.trunk.rx.character.CharacterObservable;
import com.trunk.rx.json.JsonObjectEvent;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.operator.OperatorJsonToken;
import com.trunk.rx.json.path.JsonPath;

import rx.Observable;

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

  public TransformerRxJson lenient() {
    return new TransformerRxJson(operatorJsonToken.lenient(), transformerJsonPath.lenient());
  }

  public TransformerRxJson strict() {
    return new TransformerRxJson(operatorJsonToken.strict(), transformerJsonPath.strict());
  }

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
