package com.trunk.rx.json.transformer;

import com.trunk.rx.json.JsonObjectEvent;
import com.trunk.rx.json.operator.OperatorCollectObjects;

import rx.Observable;

public class TransformerCollectObjects implements Observable.Transformer<String, JsonObjectEvent> {
  private final TransformerRxJson transformerRxJson;

  public TransformerCollectObjects(TransformerRxJson transformerRxJson) {
    this.transformerRxJson = transformerRxJson;
  }

  @Override
  public Observable<JsonObjectEvent> call(Observable<String> upstream) {
    return upstream.compose(transformerRxJson)
      .lift(new OperatorCollectObjects());
  }
}
