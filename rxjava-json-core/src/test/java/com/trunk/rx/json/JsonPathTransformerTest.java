package com.trunk.rx.json;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.trunk.rx.json.impl.CharacterIndex;
import com.trunk.rx.string.StringObservable;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.path.RootToken;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonDocumentEnd;
import com.trunk.rx.json.token.JsonObject;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.*;

public class JsonPathTransformerTest {
  @Test
  public void shouldSkipAllWhenNoMatches() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(JsonTokenOperatorTest.bigObject())
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.zzz")))
      .flatMap(e -> e.getTokens())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(JsonDocumentEnd.INSTANCE);
  }

  @Test
  public void shouldReturnSingleGroupForRootPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(JsonTokenOperatorTest.bigObject())
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.INSTANCE, NoopToken.INSTANCE);
  }

  @Test
  public void shouldReturnAllElementsForRootPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(JsonTokenOperatorTest.bigObject())
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$")))
      .flatMap(e -> e.getTokens())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      Observable.just(JsonTokenOperatorTest.bigObject())
        .lift(StringObservable.toCharacter())
        .lift(JsonTokenOperator.strict())
        .map(t -> t.getToken())
        .toList()
        .toBlocking()
        .single()
    );
  }

  @Test
  public void shouldReturnSingleGroupForAllPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(JsonTokenOperatorTest.bigObject())
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$..*")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.INSTANCE, NoopToken.INSTANCE);
  }

  @Test
  public void shouldReturnAllElementsForAllPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(JsonTokenOperatorTest.bigObject())
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$..*")))
      .flatMap(e -> e.getTokens())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      StringObservable.from(JsonTokenOperatorTest.bigObject())
        .lift(JsonTokenOperator.strict())
        .map(t -> t.getToken())
        .toList()
        .toBlocking()
        .single()
    );
  }

  @Test
  public void shouldReturnGroupForSingleMatch() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":{\"b\":[1,2,3,4,5,6]}}")
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.a.b")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(JsonPath.parse("$.a.b"));
  }

  @Test
  public void shouldReturnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.b[*]")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(JsonPath.parse("$.b[0]"), JsonPath.parse("$.b[1]"), JsonPath.parse("$.b[2]"), JsonPath.parse("$.b[3]"));
  }

  @Test
  public void shouldReturnCompleteCorrectlyOnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.b[*]")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .take(1)
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(JsonPath.parse("$.b[0]"));
  }

  @Test
  public void shouldExitEarlyWhenLastPossibleMatchMade() throws Exception {
    StringBuilder buf = new StringBuilder();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(StringObservable.toCharacter())
//    StringObservable.from(
//        "{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}"
//      )
      .doOnNext(c -> buf.append(c))
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.b[*]")))
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe();

    assertEquals(buf.toString(), "{\"a\":1234,\"b\":[1,2,3,4]");
  }

  @Test
  public void shouldNotExitEarlyWhenLenient() throws Exception {
    StringBuilder buf = new StringBuilder();
    String value = "{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}";
    Observable.just(value)
      .lift(StringObservable.toCharacter())
      .doOnNext(c -> buf.append(c))
      .lift(JsonTokenOperator.strict())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.b[*]")).lenient())
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe();

    assertEquals(buf.toString(), value);
  }

  @Test
  public void shouldTestMultipleDocumentsWhenLenient() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    String value = "{\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}}";
    Observable.just(value)
      .lift(StringObservable.toCharacter())
      .lift(JsonTokenOperator.lenient())
      .compose(JsonPathTransformer.from(JsonPath.parse("$.a.b[1]")).lenient())
      .map(e -> {
        e.getTokens().subscribe(); // need to drain group to proceed
        return e.getMatchedPathFragment();
      })
      .subscribe(ts);

    ts.assertValues(
      JsonPath.parse("$.a.b[1]"), NoopToken.INSTANCE,
      JsonPath.parse("$.a.b[1]"), NoopToken.INSTANCE,
      JsonPath.parse("$.a.b[1]"), NoopToken.INSTANCE
    );
  }

  @Test
  public void backPressureShouldWorkCorrectly() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    ts.requestMore(0);

    int[] emitted = { 0 };

    Observable.just(
      new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonArray.end(), JsonPath.parse("$"))
    )
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(JsonPathTransformer.from(JsonPath.parse("$..*")))
      .flatMap(e -> e.getTokens())
      .subscribe(ts);

    ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 1);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 2);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 3);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 4);

    ts.assertCompleted();
  }

  @Test
  public void backPressureShouldWorkCorrectlyOnPathGroups() throws Exception {
    TestSubscriber<JsonPathEvent> ts = new TestSubscriber<>();
    //ts.requestMore(0);

    int[] emitted = { 0 };

    Observable.just(
      new JsonTokenEvent(JsonArray.start(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[0]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[1]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[1]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[2]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[2]")),
      new JsonTokenEvent(JsonObject.start(), JsonPath.parse("$[3]")),
      new JsonTokenEvent(JsonObject.end(), JsonPath.parse("$[3]")),
      new JsonTokenEvent(JsonArray.end(), JsonPath.parse("$"))
    )
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(JsonPathTransformer.from(JsonPath.parse("$[*]")))
      .doOnNext(e -> e.getTokens().subscribe())
      .subscribe(ts);

 //   ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
//    assertEquals(emitted[0], 1);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 2);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 3);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 4);

    ts.assertCompleted();
  }
}
