package com.trunk.rx.json.gson.operator;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.exception.MalformedJsonException;
import com.trunk.rx.json.gson.GsonPathEvent;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonArray;
import com.trunk.rx.json.token.JsonBoolean;
import com.trunk.rx.json.token.JsonDocumentEnd;
import com.trunk.rx.json.token.JsonName;
import com.trunk.rx.json.token.JsonNull;
import com.trunk.rx.json.token.JsonNumber;
import com.trunk.rx.json.token.JsonObject;
import com.trunk.rx.json.token.JsonString;
import com.trunk.rx.json.token.JsonToken;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class OperatorJsonGsonTest {
  private static final JsonPathEvent[] EMPTY_ARRAY = {};
  public static final JsonPath PATH_A = JsonPath.parse("$.a");

  @Test
  public void shouldParseString() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonString.of("a")))
      .then(new JsonPrimitive("a"))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseLong() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonNumber.of(Long.toString(Integer.MAX_VALUE + 1L))))
      .then(new JsonPrimitive(Integer.MAX_VALUE + 1L))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseInt() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonNumber.of("1")))
      .then(new JsonPrimitive(1))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseDouble() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonNumber.of("1.1")))
      .then(new JsonPrimitive(1.1))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseExponentialNumber() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonNumber.of("3.5e+2")))
      .then(new JsonPrimitive(350))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseBoolean() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonBoolean.False()))
      .then(new JsonPrimitive(false))
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseNull() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonNull.instance()))
      .then(com.google.gson.JsonNull.INSTANCE)
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldIgnoreEndOfDocument() throws Exception {
    given(new OperatorJsonGson())
      .when(
        event(JsonString.of("a")),
        event(JsonDocumentEnd.instance()),
        event(JsonString.of("a")),
        event(JsonDocumentEnd.instance())
      )
      .then(new JsonPrimitive("a"), new JsonPrimitive("a"))
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseArray() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonArray.start()), event(JsonArray.end()))
      .then(new com.google.gson.JsonArray())
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseObject() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonObject.start()), event(JsonObject.end()))
      .then(new com.google.gson.JsonObject())
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldParseComplexObject() throws Exception {
    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
    com.google.gson.JsonArray oa = new com.google.gson.JsonArray();

    oa.add("foo");
    com.google.gson.JsonObject oa1 = new com.google.gson.JsonObject();
    com.google.gson.JsonArray oa1x = new com.google.gson.JsonArray();
    oa1x.add(5);
    oa1x.add(6);
    oa1.add("x", oa1x);
    oa.add(oa1);
    oa.add("bar");
    o.add("a", oa);
    JsonPrimitive ob = new JsonPrimitive("baz");
    o.add("b", ob);

    given(new OperatorJsonGson())
      .when(
        event(JsonObject.start()),
        event(JsonName.of("a")),
        event(JsonArray.start(), JsonPath.parse("$.a.a")),
        event(JsonString.of("foo"), JsonPath.parse("$.a.a[0]")),
        event(JsonObject.start(), JsonPath.parse("$.a.a[1]")),
        event(JsonName.of("x"), JsonPath.parse("$.a.a[1]")),
        event(JsonArray.start(), JsonPath.parse("$.a.a[1].x")),
        event(JsonNumber.of("5"), JsonPath.parse("$.a.a[1].x[0]")),
        event(JsonNumber.of("6"), JsonPath.parse("$.a.a[1].x[1]")),
        event(JsonArray.end(), JsonPath.parse("$.a.a[1].x")),
        event(JsonObject.end(), JsonPath.parse("$.a.a[1]")),
        event(JsonString.of("bar"), JsonPath.parse("$.a.a[2]")),
        event(JsonArray.end(), JsonPath.parse("$.a.a")),
        event(JsonName.of("b")),
        event(JsonString.of("baz"), JsonPath.parse("$.a.b")),
        event(JsonObject.end())
      )
      .then(o)
      .thenPath(PATH_A)
      .then(Is.COMPLETED)
      .run();
  }

  @Test
  public void shouldErrorOnBadJson() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonObject.start()), event(JsonArray.end()))
      .then()
      .thenPath()
      .then(MalformedJsonException.class)
      .run();
  }

  @Test
  public void shouldEmitObjectBeforeError() throws Exception {
    given(new OperatorJsonGson())
      .when(event(JsonString.of("a")), event(JsonObject.start()), event(JsonArray.end()))
      .then(new JsonPrimitive("a"))
      .thenPath(PATH_A)
      .then(MalformedJsonException.class)
      .run();

  }

  @Test
  public void shouldStopEmittingEarly() throws Exception {
    TestSubscriber<GsonPathEvent> ts = new TestSubscriber<>();
    int[] emitted = {0};
    Observable.just(event(JsonArray.start()), event(JsonArray.end()), event(JsonArray.start()), event(JsonArray.end()), event(JsonArray.start()), event(JsonArray.end()))
      .doOnNext(e -> emitted[0] += 1)
      .lift(new OperatorJsonGson())
      .take(2)
      .subscribe(ts);

    assertEquals(emitted[0], 4);
    assertEquals(ts.getOnNextEvents().size(), 2);
  }

  @Test
  public void shouldPassBackpressureUpstream() throws Exception {
    TestSubscriber<GsonPathEvent> ts = new TestSubscriber<>();
    ts.requestMore(0);
    int[] emitted = {0};
    Observable.just(
      event(JsonArray.start()), event(JsonArray.end()),
      event(JsonObject.start()), event(JsonObject.end()),
      event(JsonArray.start()), event(JsonArray.end()),
      event(JsonObject.start()), event(JsonObject.end())
    )
      .doOnNext(e -> emitted[0] += 1)
      .lift(new OperatorJsonGson())
      .subscribe(ts);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertNotCompleted();
    assertEquals(emitted[0], 2);
    assertEquals(ts.getOnNextEvents().size(), 1);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertNotCompleted();
    assertEquals(emitted[0], 4);
    assertEquals(ts.getOnNextEvents().size(), 2);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertNotCompleted();
    assertEquals(emitted[0], 6);
    assertEquals(ts.getOnNextEvents().size(), 3);

    ts.requestMore(1);
    ts.assertNoErrors();
    ts.assertCompleted();
    assertEquals(emitted[0], 8);
    assertEquals(ts.getOnNextEvents().size(), 4);
  }

  private JsonPathEvent event(JsonToken token) {
    return new JsonPathEvent(PATH_A, new JsonTokenEvent(token, PATH_A));
  }

  private JsonPathEvent event(JsonToken token, JsonPath path) {
    return new JsonPathEvent(PATH_A, new JsonTokenEvent(token, path));
  }

  public static TestItem should(String description) {
    return new TestItem(
      EMPTY_ARRAY,
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      description,
      null
    );
  }

  public static TestItem given(OperatorJsonGson operatorJsonGson) {
    return new TestItem(
      EMPTY_ARRAY,
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      "",
      operatorJsonGson
    );
  }

  private enum Is {
    COMPLETED,
    NOT_COMPLETED
  }

  private static class TestItem {

    private final JsonPathEvent[] tokens;
    private final Optional<JsonElement[]> expectedObjects;
    private final Optional<JsonPath[]> expectedPaths;
    private final Optional<Class> error;
    private final Optional<String> message;
    private final Optional<Is> completed;
    private final OperatorJsonGson operatorGson;
    private final String description;

    private TestItem(JsonPathEvent[] tokens,
                     Optional<JsonElement[]> expectedObjects,
                     Optional<JsonPath[]> expectedPaths, Optional<Class> error,
                     Optional<String> message,
                     Optional<Is> completed,
                     String description,
                     OperatorJsonGson operatorGson) {
      this.tokens = tokens;
      this.expectedObjects = expectedObjects;
      this.expectedPaths = expectedPaths;
      this.error = error;
      this.message = message;
      this.completed = completed;
      this.description = description;
      this.operatorGson = operatorGson;
    }

    public TestItem given(OperatorJsonGson operatorJsonToken) {
      return new TestItem(tokens, expectedObjects, expectedPaths, error, message, completed, description, operatorJsonToken);
    }

    public TestItem when(JsonPathEvent... jsonFragments) {
      return new TestItem(jsonFragments, expectedObjects, expectedPaths, error, message, completed, description, operatorGson);
    }

    public TestItem then(JsonElement... expectedObjects) {
      return new TestItem(tokens, Optional.of(expectedObjects), expectedPaths, error, message, completed, description, operatorGson);
    }

    public TestItem thenPath(JsonPath... expectedPaths) {
      return new TestItem(tokens, expectedObjects, Optional.of(expectedPaths), error, message, completed, description, operatorGson);
    }

    public TestItem then(Class<? extends Throwable> error) {
      return new TestItem(tokens, expectedObjects, expectedPaths, Optional.of(error), message, completed, description, operatorGson);
    }

    public TestItem then(Is completed) {
      return new TestItem(tokens, expectedObjects, expectedPaths, error, message, Optional.of(completed), description, operatorGson);
    }

    public TestItem then(String message) {
      return new TestItem(tokens, expectedObjects, expectedPaths, error, Optional.of(message), completed, description, operatorGson);
    }

    public void run() {
      try {
        TestSubscriber<GsonPathEvent> ts = new TestSubscriber<>();
        Observable.from(tokens)
          .lift(operatorGson)
          .subscribe(ts);

        ts.awaitTerminalEvent(2, TimeUnit.SECONDS);

        error.ifPresent(ts::assertError);
        message.ifPresent(
          m ->
            assertEquals(ts.getOnErrorEvents().get(0).getMessage(), m)
        );
        completed.ifPresent(c -> {
          if (c == Is.COMPLETED) {
            ts.assertNoErrors();
            ts.assertCompleted();
          } else {
            ts.assertNotCompleted();
          }
        });
        expectedObjects.ifPresent(
          o ->
            assertEquals(
              ts.getOnNextEvents().stream().map(GsonPathEvent::getElement).collect(Collectors.toList()),
              ImmutableList.copyOf(o)
            )
        );
        expectedPaths.ifPresent(
          o ->
            assertEquals(
              ts.getOnNextEvents().stream().map(GsonPathEvent::getPath).collect(Collectors.toList()),
              ImmutableList.copyOf(o)
            )
        );
      } catch (Throwable t) {
        String json = String.join("", Arrays.stream(tokens).map(e -> e.getTokenEvent().getToken().value()).collect(Collectors.toList()));
        Assert.fail((description.isEmpty() ? "" : "should " + description + " ") + "'" + json.substring(0, Math.min(json.length(), 120)) + "'", t);
      }
    }
  }

}
