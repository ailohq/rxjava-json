package com.trunk.rx.json.gson.transformer;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import rx.Observable;
import rx.observers.TestSubscriber;

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
}
