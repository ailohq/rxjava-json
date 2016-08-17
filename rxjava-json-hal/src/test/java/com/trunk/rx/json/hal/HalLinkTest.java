package com.trunk.rx.json.hal;

import com.trunk.rx.json.RxJson;
import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observables.StringObservable;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.Locale;

public class HalLinkTest {

  @Test
  public void shouldSerializeHrefOnly() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")), 
        "{\"href\":\"/test\"}"
    );
  }

  @Test
  public void shouldAcceptTemplated() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).templated(true),
        "{\"href\":\"/test\",\"templated\":true}"
    );
  }

  @Test
  public void shouldAcceptType() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).type("text/*"),
        "{\"href\":\"/test\",\"type\":\"text/*\"}"
    );
  }

  @Test
  public void shouldAcceptDeprecation() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).deprecation("Do not use plz"),
        "{\"deprecation\":\"Do not use plz\",\"href\":\"/test\"}"
    );
  }

  @Test
  public void shouldAcceptName() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).name("A name"),
        "{\"href\":\"/test\",\"name\":\"A name\"}"
    );
  }

  @Test
  public void shouldAcceptProfile() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).profile(URI.create("/prof")),
        "{\"href\":\"/test\",\"profile\":\"/prof\"}"
    );
  }

  @Test
  public void shouldAcceptTitle() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).title("A nice title"),
        "{\"href\":\"/test\",\"title\":\"A nice title\"}"
    );
  }

  @Test
  public void shouldAcceptHreflang() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test")).hreflang(Locale.CANADA_FRENCH),
        "{\"href\":\"/test\",\"hreflang\":\"fr_CA\"}"
    );
  }

  @Test
  public void shouldSerializeFullLink() throws Exception {
    assertEquals(
        HalLink.create(URI.create("/test"))
            .templated(false)
            .type("application/xml; charset=utf-8")
            .deprecation("Do not use plz")
            .name("A name")
            .profile(URI.create("/prof"))
            .title("A nice title")
            .hreflang(Locale.CANADA_FRENCH)
            ,
        "{" +
            "\"deprecation\":\"Do not use plz\"," +
            "\"href\":\"/test\"," +
            "\"hreflang\":\"fr_CA\"," +
            "\"name\":\"A name\"," +
            "\"profile\":\"/prof\"," +
            "\"templated\":false," +
            "\"title\":\"A nice title\"," +
            "\"type\":\"application/xml; charset=utf-8\"" +
            "}"
    );
  }

  private void assertEquals(Observable<JsonToken> in, String out) {
    TestSubscriber<String> ts = new TestSubscriber<>();

    in.compose(toJson).subscribe(ts);

    ts.assertCompleted();
    ts.assertValue(out);

  } 
  
  private Observable.Transformer<JsonToken, String> toJson = stringObservable -> StringObservable.stringConcat(stringObservable.compose(RxJson.toJson()));

}