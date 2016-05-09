package com.trunk.rx.json.transformer;

import org.testng.annotations.Test;

import com.trunk.rx.character.CharacterObservable;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.operator.OperatorJsonToken;
import com.trunk.rx.json.operator.OperatorJsonTokenTest;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.path.NoopToken;
import com.trunk.rx.json.path.RootToken;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonDocumentEnd;
import com.trunk.rx.json.token.JsonObject;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.testng.Assert.assertEquals;

public class TransformerJsonPathTest {

  public static final OperatorJsonToken STRICT_PARSER = new OperatorJsonToken();
  public static final OperatorJsonToken LENIENT_PARSER = STRICT_PARSER.lenient();

  @Test
  public void shouldSkipAllWhenNoMatches() throws Exception {
    TestSubscriber<JsonPathEvent> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.zzz")))
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(new JsonPathEvent(NoopToken.INSTANCE, new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE)));
  }

  @Test
  public void shouldReturnSingleGroupForRootPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$")))
      .map(e -> e.getMatchedPathFragment())
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.INSTANCE, NoopToken.INSTANCE);
  }

  @Test
  public void shouldReturnAllElementsForRootPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      Observable.just(OperatorJsonTokenTest.bigObject())
        .lift(CharacterObservable.toCharacter())
        .lift(STRICT_PARSER)
        .map(t -> t.getToken())
        .toList()
        .toBlocking()
        .single()
    );
  }

  @Test
  public void shouldReturnSingleGroupForAllPath() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getMatchedPathFragment())
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(RootToken.INSTANCE, NoopToken.INSTANCE);
  }

  @Test
  public void shouldReturnAllElementsForAllPath() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    Observable.just(OperatorJsonTokenTest.bigObject())
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(
      ts.getOnNextEvents(),
      CharacterObservable.from(OperatorJsonTokenTest.bigObject())
        .lift(STRICT_PARSER)
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
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.a.b")))
      .map(e -> e.getMatchedPathFragment())
      .distinctUntilChanged()
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValue(JsonPath.parse("$.a.b"));
  }

  @Test
  public void shouldReturnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(e -> e.getMatchedPathFragment())
      .subscribe(ts);

    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(JsonPath.parse("$.b[0]"), JsonPath.parse("$.b[1]"), JsonPath.parse("$.b[2]"), JsonPath.parse("$.b[3]"));
  }

  @Test
  public void shouldReturnCompleteCorrectlyOnManyMatches() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    Observable.just("{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}")
      .lift(CharacterObservable.toCharacter())
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(e -> e.getMatchedPathFragment())
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
      .lift(CharacterObservable.toCharacter())
      .doOnNext(c -> buf.append(c))
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")))
      .map(e -> e.getMatchedPathFragment())
      .subscribe();

    assertEquals(buf.toString(), "{\"a\":1234,\"b\":[1,2,3,4]");
  }

  @Test
  public void shouldNotExitEarlyWhenLenient() throws Exception {
    StringBuilder buf = new StringBuilder();
    String value = "{\"a\":1234,\"b\":[1,2,3,4],\"c\":{\"w\":[5,6,7,8],\"x\":true,\"y\":false,\"z\":null},\"d\":[{\"1\":\"1\"}]}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .doOnNext(c -> buf.append(c))
      .lift(STRICT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.b[*]")).lenient())
      .map(e -> e.getMatchedPathFragment())
      .subscribe();

    assertEquals(buf.toString(), value);
  }

  @Test
  public void shouldTestExitOnFirstDocumentDocumentsWhenStrict() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    String value = "{}{}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .lift(LENIENT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getMatchedPathFragment())
      .subscribe(ts);

    ts.assertValues(
      JsonPath.parse("$"), JsonPath.parse("$"), NoopToken.INSTANCE
    );
  }

  @Test
  public void shouldTestMultipleDocumentsWhenLenient() throws Exception {
    TestSubscriber<JsonPath> ts = new TestSubscriber<>();
    String value = "{\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}} {\"a\":{\"b\":[1,2,3,4,5,6]}}";
    Observable.just(value)
      .lift(CharacterObservable.toCharacter())
      .lift(LENIENT_PARSER)
      .compose(TransformerJsonPath.from(JsonPath.parse("$.a.b[1]")).lenient())
      .map(e -> e.getMatchedPathFragment())
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
      new JsonTokenEvent(JsonArray.end(), JsonPath.parse("$")),
      new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE)
    )
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(TransformerJsonPath.from(JsonPath.parse("$..*")))
      .map(e -> e.getTokenEvent().getToken())
      .subscribe(ts);

    ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 2); // concatMap is eager on the next element

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 3);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 4);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 5);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 5);
    assertEquals(emitted[0], 5);

    ts.assertCompleted();
  }

  @Test
  public void backPressureShouldWorkCorrectlyOnPathGroups() throws Exception {
    TestSubscriber<JsonPathEvent> ts = new TestSubscriber<>();
    ts.requestMore(0);

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
    ).concatWith(Observable.just(new JsonTokenEvent(JsonDocumentEnd.INSTANCE, NoopToken.INSTANCE)))
      .doOnNext(t -> emitted[0] = emitted[0] + 1)
      .compose(TransformerJsonPath.from(JsonPath.parse("$[*]")))
      .subscribe(ts);

    ts.assertNoValues();

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 1);
    assertEquals(emitted[0], 3); // concatMap is eager on the next element

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 2);
    assertEquals(emitted[0], 4);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 3);
    assertEquals(emitted[0], 5);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 4);
    assertEquals(emitted[0], 6);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 5);
    assertEquals(emitted[0], 7);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 6);
    assertEquals(emitted[0], 8);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 7);
    assertEquals(emitted[0], 9);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 8);
    assertEquals(emitted[0], 10);

    ts.requestMore(1);
    assertEquals(ts.getOnNextEvents().size(), 9);
    assertEquals(emitted[0], 11);

    ts.assertCompleted();
  }
}
