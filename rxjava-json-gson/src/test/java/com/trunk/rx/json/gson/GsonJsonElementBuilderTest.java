package com.trunk.rx.json.gson;

import com.google.gson.GsonBuilder;
import com.trunk.rx.json.token.JsonRaw;
import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public class GsonJsonElementBuilderTest {
  @Test
  public void shouldUseDefaultGson() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    new GsonJsonElementBuilder()
        .toJson("foo")
        .subscribe(ts);

    ts.assertCompleted();
    ts.assertValue(JsonRaw.of("\"foo\""));
  }

  @Test
  public void shouldSupportCustomGson() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    new GsonJsonElementBuilder(new GsonBuilder().serializeSpecialFloatingPointValues().create())
        .toJson(Double.POSITIVE_INFINITY)
        .subscribe(ts);

    ts.assertCompleted();
    ts.assertValue(JsonRaw.of("Infinity"));
  }
}