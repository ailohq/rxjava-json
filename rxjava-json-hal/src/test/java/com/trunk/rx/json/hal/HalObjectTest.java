package com.trunk.rx.json.hal;

import com.google.gson.Gson;
import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.element.JsonObject;
import com.trunk.rx.json.element.JsonValueBuilder;
import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.Collections;

import static rx.Observable.just;

public class HalObjectTest {

  public static final JsonValueBuilder VALUE_BUILDER = JsonValueBuilder.instance();
  private Gson gson = new Gson();

  @Test
  public void shouldReturnEmptyObject() throws Exception {
    assertEquals(
      HalObject.create(),
      "{}"
    );
  }

  @Test
  public void shouldAllowJustSelf() throws Exception {
    assertEquals(
      HalObject.create().self(HalLink.create(URI.create("/me"))),
      "{\"_links\":{\"self\":{\"href\":\"/me\"}}}"
    );
  }

  @Test
  public void shouldUpdateSelf() throws Exception {
    assertEquals(
      HalObject.create().self(HalLink.create(URI.create("/me"))).self(HalLink.create(URI.create("/me_too"))),
      "{\"_links\":{\"self\":{\"href\":\"/me_too\"}}}"
    );
  }

  @Test(expectedExceptions = HalKeyException.class, expectedExceptionsMessageRegExp = "Rel 'self' can only be set using #self")
  public void shouldNotAllowSelfToBeSetViaAdd() throws Exception {
    HalObject.create().putLink("self", HalLink.create(URI.create("/me")));
  }

  @Test
  public void shouldAddSingleLink() throws Exception {
    assertEquals(
      HalObject.create().putLink("foo", HalLink.create(URI.create("/bar"))),
      "{\"_links\":{\"foo\":{\"href\":\"/bar\"}}}"
    );
  }

  @Test
  public void shouldAddArrayLink() throws Exception {
    assertEquals(
      HalObject.create().putLinks("foo", HalLink.create(URI.create("/bar"))),
      "{\"_links\":{\"foo\":[{\"href\":\"/bar\"}]}}"
    );
  }

  @Test
  public void shouldAddObservableLink() throws Exception {
    assertEquals(
      HalObject.create().putLinks("foo", Observable.just(HalLink.create(URI.create("/bar")))),
      "{\"_links\":{\"foo\":[{\"href\":\"/bar\"}]}}"
    );
  }

  @Test
  public void shouldAddSingleEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded("foo", HalObject.create().putLink("bar", HalLink.create(URI.create("/baz"))))
        ,
      "{\"_embedded\":{\"foo\":{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}}}"
    );
  }

  @Test
  public void shouldAddArrayEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded("foo", Collections.singletonList(HalObject.create().putLink("bar", HalLink.create(URI.create("/baz")))))
        ,
      "{\"_embedded\":{\"foo\":[{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}]}}"
    );
  }

  @Test
  public void shouldAddObservableEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded(
          "foo",
          just(HalObject.create().putLink("bar", HalLink.create(URI.create("/baz"))))
        )
        ,
      "{\"_embedded\":{\"foo\":[{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}]}}"
    );
  }

  @Test
  public void shouldAddSingleData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("foo", VALUE_BUILDER.create("bar"))
        ,
      "{\"foo\":\"bar\"}"
    );
  }

  @Test
  public void shouldAddObservableData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("foo", VALUE_BUILDER.create("bar"))
        ,
      "{\"foo\":\"bar\"}"
    );
  }

  @Test
  public void shouldAddLinkAndEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded("foo", HalObject.create().putLink("bar", HalLink.create(URI.create("/baz"))))
        .putLink("foo", HalLink.create(URI.create("/bar")))
        ,
      "{\"_links\":{\"foo\":{\"href\":\"/bar\"}},\"_embedded\":{\"foo\":{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}}}"
    );
  }

  @Test
  public void shouldAddLinkAndData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("foo", VALUE_BUILDER.create("bar"))
        .putLink("foo", HalLink.create(URI.create("/bar")))
        ,
      "{\"_links\":{\"foo\":{\"href\":\"/bar\"}},\"foo\":\"bar\"}"
    );
  }

  @Test
  public void shouldAddEmbeddedAndData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("foo", VALUE_BUILDER.create("bar"))
        .putEmbedded("foo", HalObject.create().putLink("bar", HalLink.create(URI.create("/baz"))))
        ,
      "{\"_embedded\":{\"foo\":{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}},\"foo\":\"bar\"}"
    );
  }

  @Test
  public void shouldAddLinkEmbeddedAndData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("foo", VALUE_BUILDER.create("bar"))
        .putEmbedded("foo", HalObject.create().putLink("bar", HalLink.create(URI.create("/baz"))))
        .putLink("foo", HalLink.create(URI.create("/bar")))
        .self(HalLink.create(URI.create("/me")))
        ,
      "{\"_links\":{\"self\":{\"href\":\"/me\"},\"foo\":{\"href\":\"/bar\"}},\"_embedded\":{\"foo\":{\"_links\":{\"bar\":{\"href\":\"/baz\"}}}},\"foo\":\"bar\"}"
    );
  }

  @Test(expectedExceptions = HalKeyException.class, expectedExceptionsMessageRegExp = "Data cannot use reserved property '_links'")
  public void shouldErrorOn_linksDataKey() throws Exception {
    HalObject.create().appendData("_links", VALUE_BUILDER.create(1));
  }

  @Test(expectedExceptions = HalKeyException.class, expectedExceptionsMessageRegExp = "Data cannot use reserved property '_embedded'")
  public void shouldErrorOn_embeddedDataKey() throws Exception {
    HalObject.create().appendData("_embedded", VALUE_BUILDER.create(1));
  }

  @Test
  public void shouldAllowSingleThenObservableLinks() throws Exception {
    assertEquals(
      HalObject.create()
        .putLink("c", HalLink.create(URI.create("/3")))
        .putLink("d", HalLink.create(URI.create("/4")))
        .putLinks("a",
          just(
            HalLink.create(URI.create("/1")),
            HalLink.create(URI.create("/2"))
          )
        )
        ,
      "{\"_links\":{\"c\":{\"href\":\"/3\"},\"d\":{\"href\":\"/4\"},\"a\":[{\"href\":\"/1\"},{\"href\":\"/2\"}]}}"
    );
  }

  @Test
  public void shouldAllowObservableThenSingleLinks() throws Exception {
    assertEquals(
      HalObject.create()
        .putLinks("a",
          just(
            HalLink.create(URI.create("/1")),
            HalLink.create(URI.create("/2"))
          )
        )
        .putLink("c", HalLink.create(URI.create("/3")))
        .putLink("d", HalLink.create(URI.create("/4")))
        ,
      "{\"_links\":{\"a\":[{\"href\":\"/1\"},{\"href\":\"/2\"}],\"c\":{\"href\":\"/3\"},\"d\":{\"href\":\"/4\"}}}"
    );
  }

  @Test
  public void shouldAllowSingleThenObservableEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded("c", HalObject.create().putLink("3", HalLink.create(URI.create("/3"))))
        .putEmbedded("d", HalObject.create().putLink("4", HalLink.create(URI.create("/4"))))
        .putEmbedded("a",
          just(
            HalObject.create().putLink("1", HalLink.create(URI.create("/1"))),
            HalObject.create().putLink("2", HalLink.create(URI.create("/2")))
          )
        )
        ,
      "{\"_embedded\":{\"c\":{\"_links\":{\"3\":{\"href\":\"/3\"}}},\"d\":{\"_links\":{\"4\":{\"href\":\"/4\"}}},\"a\":[{\"_links\":{\"1\":{\"href\":\"/1\"}}},{\"_links\":{\"2\":{\"href\":\"/2\"}}}]}}"
    );
  }

  @Test
  public void shouldAllowObservableThenSingleEmbedded() throws Exception {
    assertEquals(
      HalObject.create()
        .putEmbedded("a",
          just(
            HalObject.create().putLink("1", HalLink.create(URI.create("/1"))),
            HalObject.create().putLink("2", HalLink.create(URI.create("/2")))
          )
        )
        .putEmbedded("c", HalObject.create().putLink("3", HalLink.create(URI.create("/3"))))
        .putEmbedded("d", HalObject.create().putLink("4", HalLink.create(URI.create("/4"))))
        ,
      "{\"_embedded\":{\"c\":{\"_links\":{\"3\":{\"href\":\"/3\"}}},\"d\":{\"_links\":{\"4\":{\"href\":\"/4\"}}},\"a\":[{\"_links\":{\"1\":{\"href\":\"/1\"}}},{\"_links\":{\"2\":{\"href\":\"/2\"}}}]}}"
    );
  }

  // {

  @Test
  public void shouldAllowSingleThenObservableData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData("c", VALUE_BUILDER.create(3))
        .appendData("d", VALUE_BUILDER.create(4))
        .appendData(
          just(
            JsonObject.entry("a", VALUE_BUILDER.create(1)),
            JsonObject.entry("b", VALUE_BUILDER.create(2))
          )
        )
        ,
      "{\"c\":3,\"d\":4,\"a\":1,\"b\":2}"
    );
  }

  @Test
  public void shouldAllowObservableThenSingleData() throws Exception {
    assertEquals(
      HalObject.create()
        .appendData(
          just(
            JsonObject.entry("a", VALUE_BUILDER.create(1)),
            JsonObject.entry("b", VALUE_BUILDER.create(2))
          )
        )
      .appendData("c", VALUE_BUILDER.create(3))
      .appendData("d", VALUE_BUILDER.create(4))
      ,
      "{\"a\":1,\"b\":2,\"c\":3,\"d\":4}"
    );
  }

  private void assertEquals(Observable<JsonToken> in, String out) {
    TestSubscriber<Object> ts = new TestSubscriber<>();

    in.compose(RxJson.toJson()).compose(StringObservable::stringConcat).map(s -> gson.fromJson(s, Object.class)).subscribe(ts);

    ts.assertCompleted();
    ts.assertValue(gson.fromJson(out, Object.class));
  }
}
