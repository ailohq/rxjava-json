package com.trunk.rx.json.operator;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.trunk.rx.json.JsonObjectEvent;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonDocumentEnd;
import com.trunk.rx.json.token.JsonString;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.*;

public class OperatorCollectObjectsTest {
  @Test
  public void shouldCollectSingleObject() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonArray.start(), NoopToken.INSTANCE))
    )
      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
  }

  @Test
  public void shouldCollectObjects() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
    )
      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      ),
      new JsonObjectEvent(
        JsonPath.parse("$.b"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))
        )
      )
    );
  }

  @Test
  public void shouldCollectSamePathInDifferentDocumentsToDifferentObjects() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE)),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
    )
      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      ),
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
  }

  @Test
  public void shouldDropCurrentObjectOnError() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    RuntimeException exception = new RuntimeException();
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b")))
    )
      .concatWith(Observable.error(exception))
      .concatWith(
        Observable.just(
          new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]"))),
          new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))),
          new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
        )
      )

      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.assertError(exception);
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
  }

  @Test
  public void shouldPassOnError() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    RuntimeException exception = new RuntimeException();
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
    )
      .concatWith(Observable.error(exception))
      .concatWith(
        Observable.just(
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
          new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
        )
      )

      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.assertError(exception);
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
  }

  @Test
  public void shouldApplyBackPressure() throws Exception {
    TestSubscriber<JsonObjectEvent> ts = new TestSubscriber<>();
    ts.requestMore(0);
    int[] emitted = {0};
    Observable.just(
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
      new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]"))),
      new JsonPathEvent(JsonPath.parse("$.b"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))),
      new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
    )
      .concatWith(
        Observable.just(
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]"))),
          new JsonPathEvent(JsonPath.parse("$.a"), new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))),
          new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE))
        )
      )
      .doOnNext(e -> emitted[0] += 1)
      .lift(new OperatorCollectObjects())
      .subscribe(ts);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertNotCompleted();
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
    assertEquals(emitted[0], 4);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertNotCompleted();
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      ),
      new JsonObjectEvent(
        JsonPath.parse("$.b"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))
        )
      )
    );
    assertEquals(emitted[0], 7);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      ),
      new JsonObjectEvent(
        JsonPath.parse("$.b"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.b[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.b"))
        )
      ),
      new JsonObjectEvent(
        JsonPath.parse("$.a"),
        ImmutableList.of(
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a")),
          new JsonTokenEvent(JsonString.of("a"), JsonPath.parse("$.a[0]")),
          new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$.a"))
        )
      )
    );
    assertEquals(emitted[0], 11);
  }
}
