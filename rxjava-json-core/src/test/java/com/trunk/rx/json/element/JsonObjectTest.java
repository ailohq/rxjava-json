package com.trunk.rx.json.element;

import com.google.gson.Gson;
import org.testng.annotations.Test;
import rx.Observable;

import static com.trunk.rx.json.Assert.assertEquals;

public class JsonObjectTest {
  private Gson gson = new Gson();

  @Test
  public void shouldAcceptSingleValue() throws Exception {
    assertEquals(
      JsonObject.of()
        .add("a", JsonValueBuilder.instance().create("b")),
      "{\"a\":\"b\"}"
    );
  }

  @Test
  public void shouldAcceptObservableValue() throws Exception {
    assertEquals(
      JsonObject.of()
        .add(
          "a",
          Observable.just(
            JsonValueBuilder.instance().create("b"),
            JsonValueBuilder.instance().create("c")
          )
        ),
      "{\"a\":\"b\"}"
    );
  }

  @Test
  public void shouldAcceptEmptyObservable() throws Exception {
    assertEquals(
      JsonObject.of().add("a", Observable.empty()),
      "{\"a\":null}"
    );
  }
}