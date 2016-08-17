package com.trunk.rx.json.gson.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.element.JsonArray;
import com.trunk.rx.json.gson.RxJsonGson;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

import java.util.Collections;

public class TransformerRxJsonGsonTest {
  @Test
  public void shouldMarshallDefaultObjects() throws Exception {
    TestSubscriber<Object> ts = new TestSubscriber<>();
    Observable.just("{\"a\":[1,2,3,{\"x\":[\"4\"]}]}")
      .compose(TransformerRxJsonGson.from("$.a[*]"))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(1.0, 2.0, 3.0, ImmutableMap.of("x", ImmutableList.of("4")));
  }

  @Test
  public void shouldEmitComplexObjects() throws Exception {
    TestSubscriber<String> ts = new TestSubscriber<>();

    JsonArray.of(Observable.just("foo", Collections.singletonList("bar")).compose(RxJsonGson.toJsonElements()))
      .compose(RxJson.toJson())
      .compose(StringObservable::stringConcat)
      .subscribe(ts);

    ts.assertCompleted();
    ts.assertValue("[\"foo\",[\"bar\"]]");
  }
}
