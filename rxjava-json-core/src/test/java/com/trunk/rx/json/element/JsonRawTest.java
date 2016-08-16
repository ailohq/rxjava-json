package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public class JsonRawTest {
  @Test
  public void shouldReturnSingleRawToken() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();

    JsonRaw.of("foo").subscribe(ts);

    ts.assertCompleted();
    ts.assertValue(com.trunk.rx.json.token.JsonRaw.of("foo"));
  }
}