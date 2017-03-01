package com.trunk.rx.json.element;

import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.token.JsonNull;
import org.testng.annotations.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.stream.Stream;

import static com.trunk.rx.json.Assert.assertEquals;

public class JsonObjectTest {
  @Test
  public void shouldAcceptEntries() throws Exception {
    assertEquals(
      JsonObject.of(
        Observable.just(
          JsonObject.entry("a", JsonValueBuilder.instance().create("b")),
          JsonObject.entry("c", JsonValueBuilder.instance().create("d"))
        )
      ),
      "{\"a\":\"b\", \"c\":\"d\"}"
    );
  }

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

  @Test
  public void testSuppressNullsFromEmptyValues() throws Exception {
    assertEquals(
      JsonObject.of().add("a", Observable.empty()).suppressNulls(),
      "{}"
    );
  }

  @Test
  public void testSuppressNullsFromNullValues() throws Exception {
    assertEquals(
      JsonObject.of().add("a", RxJson.valueBuilder().Null()).suppressNulls(),
      "{}"
    );
  }

  @Test
  public void testNotSuppressNullsFromValue() throws Exception {
    assertEquals(
      JsonObject.of().add("a", RxJson.valueBuilder().create("b")).suppressNulls(),
      "{\"a\":\"b\"}"
    );
  }
}