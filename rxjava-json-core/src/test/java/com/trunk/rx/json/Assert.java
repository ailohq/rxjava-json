package com.trunk.rx.json;

import com.google.gson.Gson;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

public class Assert {

  private static final Gson gson = new Gson();

  public static void assertEquals(Observable<JsonToken> in, String out) {
    TestSubscriber<Object> ts = new TestSubscriber<>();

    in.compose(RxJson.toJson())
      .compose(StringObservable::stringConcat)
      .map(s -> gson.fromJson(s, Object.class))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(gson.fromJson(out, Object.class));
  }
}
