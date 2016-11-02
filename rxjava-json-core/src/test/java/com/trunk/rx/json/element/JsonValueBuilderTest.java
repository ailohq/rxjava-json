package com.trunk.rx.json.element;

import com.trunk.rx.json.token.JsonBoolean;
import com.trunk.rx.json.token.JsonNull;
import com.trunk.rx.json.token.JsonNumber;
import com.trunk.rx.json.token.JsonQuote;
import com.trunk.rx.json.token.JsonString;
import com.trunk.rx.json.token.JsonToken;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonValueBuilderTest {

  private static final JsonValueBuilder JSON_VALUE_BUILDER = JsonValueBuilder.instance();

  @Test
  public void shouldReturnStringWithQuotes() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create("foo").subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of("foo"), JsonQuote.instance());
  }

  @Test
  public void shouldReturnTrue() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(true).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonBoolean.True());
  }

  @Test
  public void shouldReturnFalse() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(false).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonBoolean.False());
  }

  @Test
  public void shouldReturnNull() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.Null().subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNull.instance());
  }

  @Test
  public void shouldNotQuoteVeryBigNumberFromString() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.createNumberFromString("foo").subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("foo"));
  }

  @Test
  public void shouldReturnInteger() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(1).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1"));
  }

  @Test
  public void shouldReturnLong() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(Long.MAX_VALUE).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of(Long.toString(Long.MAX_VALUE)));
  }

  @Test
  public void shouldReturnAtomicInt() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(new AtomicInteger(1)).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1"));
  }

  @Test
  public void shouldQuoteLongWhenUnsafe() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.quoteLargeNumbers().create(Long.MAX_VALUE).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of(Long.toString(Long.MAX_VALUE)), JsonQuote.instance());
  }

  @Test
  public void shouldNotQuoteLongWhenSafe() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.quoteLargeNumbers().create(1L).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1"));
  }

  @Test
  public void shouldReturnBigInteger() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(BigInteger.valueOf(Long.MAX_VALUE)).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of(Long.toString(Long.MAX_VALUE)));
  }

  @Test
  public void shouldQuoteBigIntegerWhenUnsafe() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.quoteLargeNumbers().create(BigInteger.valueOf(Long.MAX_VALUE)).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of(Long.toString(Long.MAX_VALUE)), JsonQuote.instance());
  }

  @Test
  public void shouldNotQuoteBigIntegerWhenSafe() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.quoteLargeNumbers().create(BigInteger.valueOf(1L)).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1"));
  }

  @Test
  public void shouldReturnFloat() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(1.1).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1.1"));
  }

  @Test
  public void shouldReturnInfinityIfLenient() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.lenient().create(Double.NEGATIVE_INFINITY).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("-Infinity"));
  }

  @Test
  public void shouldPreferQuoteInfinityIfLenientAndQuote() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.lenient().quoteInfiniteAndNaN().create(Double.NEGATIVE_INFINITY).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of("-Infinity"), JsonQuote.instance());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldErrorInfinity() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(Double.NEGATIVE_INFINITY).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of("-Infinity"), JsonQuote.instance());
  }

  @Test
  public void shouldReturnQuoteInfinityIfQuote() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.quoteInfiniteAndNaN().create(Double.NEGATIVE_INFINITY).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonQuote.instance(), JsonString.of("-Infinity"), JsonQuote.instance());
  }

  @Test
  public void shouldReturnDouble() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    JSON_VALUE_BUILDER.create(1.1D).subscribe(ts);
    ts.assertCompleted();
    ts.assertValues(JsonNumber.of("1.1"));
  }

  @Test
  public void shouldIgnoreNulls() throws Exception {
    TestSubscriber<JsonToken> ts = new TestSubscriber<>();
    String s = null;
    Number n = null;
    JSON_VALUE_BUILDER.create(s)
      .concatWith(JSON_VALUE_BUILDER.create(n))
      .concatWith(JSON_VALUE_BUILDER.createNumberFromString(s))
      .subscribe(ts);
    ts.assertNoErrors();
    ts.assertCompleted();
    ts.assertValues(
      JsonNull.instance(),
      JsonNull.instance(),
      JsonNull.instance()
    );
  }

  @Test
  public void shouldNotQuoteMaxDouble() throws Exception {

  }

  @Test
  public void shouldQuoteBigDecimalWhenUnsafe() throws Exception {

  }

  @Test
  public void shouldNotQuoteBigDecimalWhenSafe() throws Exception {

  }
}