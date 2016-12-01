package com.trunk.rx.json.element;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import com.trunk.rx.json.token.JsonBoolean;
import com.trunk.rx.json.token.JsonNull;
import com.trunk.rx.json.token.JsonNumber;
import com.trunk.rx.json.token.JsonQuote;
import com.trunk.rx.json.token.JsonString;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;

public class JsonValueBuilder {

  private static final class Holder {
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;
    private static final BigInteger MAX_SAFE_BIG_INTEGER = BigInteger.valueOf(MAX_SAFE_INTEGER);
    private static final BigInteger MIN_SAFE_BIG_INTEGER = BigInteger.valueOf(MIN_SAFE_INTEGER);
    private static final BigInteger MAX_SAFE_MANTISSA = BigInteger.valueOf(MAX_SAFE_INTEGER);
    private static final int MAX_SAFE_EXPONENT = 1023;
    private static final int MIN_SAFE_EXPONENT = -1022;
    private static final JsonValueBuilder INSTANCE = new JsonValueBuilder();
    private static final JsonElement NULL_ELEMENT = new JsonElement(JsonNull.instance());
  }

  private final boolean lenient;
  private final boolean quoteLargeNumbers;
  private final boolean quoteInfiniteAndNaN;

  public static boolean isTooBigForJs(Number value) {
    if (value instanceof Double || value instanceof Float || value instanceof DoubleAdder ||
        value instanceof Integer || value instanceof AtomicInteger || value instanceof Short || value instanceof Byte) {
      return false;
    } else if (value instanceof BigDecimal) {
      BigDecimal decimal = (BigDecimal) value;
      int exp = decimal.precision() - decimal.scale() - 1;
      return decimal.unscaledValue().compareTo(Holder.MAX_SAFE_MANTISSA) > 0 || exp > Holder.MAX_SAFE_EXPONENT || exp < Holder.MIN_SAFE_EXPONENT;
    } else if (value instanceof BigInteger) {
      return ((BigInteger) value).compareTo(Holder.MAX_SAFE_BIG_INTEGER) > 0 || ((BigInteger) value).compareTo(Holder.MIN_SAFE_BIG_INTEGER) < 0;
    } else {
      return value.longValue() > Holder.MAX_SAFE_INTEGER || value.longValue() < Holder.MIN_SAFE_INTEGER;
    }
  }

  public static JsonValueBuilder instance() {
    return Holder.INSTANCE;
  }

  private JsonValueBuilder() {
    this(false, false, false);
  }

  private JsonValueBuilder(boolean lenient, boolean quoteLargeNumbers, boolean quoteInfiniteAndNaN) {
    this.lenient = lenient;
    this.quoteLargeNumbers = quoteLargeNumbers;
    this.quoteInfiniteAndNaN = quoteInfiniteAndNaN;
  }

  /**
   * By default, JsonValueBuilder on produces JSON as specified by
   * <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a> and does not allow non-numeric numbers like 'NaN' and 'Infinity'.
   *
   * @return a new JsonValueBuilder that has lenient number support
   */
  public JsonValueBuilder lenient() {
    return new JsonValueBuilder(true, quoteLargeNumbers, quoteInfiniteAndNaN);
  }

  /**
   * @return a new JsonValueBuilder that has strict number support
   */
  public JsonValueBuilder strict() {
    return new JsonValueBuilder(false, quoteLargeNumbers, quoteInfiniteAndNaN);
  }

  /**
   * By default, JsonValueBuilder emits large numbers in the raw. This can cause truncation problems
   * with native JavaScript conversion. This enables the quoting of all numbers that cannot be represented
   * as 64 bit doubles.
   *
   * @return a new JsonValueBuilder that will quote large numbers
   */
  public JsonValueBuilder quoteLargeNumbers() {
    return new JsonValueBuilder(lenient, true, quoteInfiniteAndNaN);
  }

  /**
   * @return a new JsonValueBuilder that will emit unquoted large numbers
   */
  public JsonValueBuilder rawLargeNumbers() {
    return new JsonValueBuilder(lenient, false, quoteInfiniteAndNaN);
  }

  /**
   * By default, JsonValueBuilder on produces JSON as specified by
   * <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a> and does not allow non-numeric numbers like 'NaN' and 'Infinity'.
   *
   * @return a new JsonValueBuilder that quotes non-numeric numbers
   */
  public JsonValueBuilder quoteInfiniteAndNaN() {
    return new JsonValueBuilder(lenient, quoteLargeNumbers, true);
  }

  /**
   * @return a new JsonValueBuilder that emits raw non-numeric numbers
   */
  public JsonValueBuilder rawInfiniteAndNaN() {
    return new JsonValueBuilder(lenient, quoteLargeNumbers, false);
  }

  /**
   * A single JSON string.
   */
  public JsonElement create(String value) {
    if (value == null) {
      return Null();
    }
    return new JsonElement(Observable.just(JsonQuote.instance(), JsonString.of(value), JsonQuote.instance()));
  }

  /**
   * Create a single JSON string from an Observable of Strings. This is to allow large Strings to be emitted
   * as a stream.
   *
   * @param values the string to be emitted as an Observable
   * @return a single string JsonElement composed of the
   */
  public JsonElement create(Observable<String> values) {
    return new JsonElement(
      Observable.<JsonToken>just(JsonQuote.instance())
        .concatWith(values.map(JsonString::of))
        .concatWith(Observable.just(JsonQuote.instance()))
    );
  }

  /**
   * A single JSON boolean.
   */
  public JsonElement create(Boolean value) {
    if (value == null) {
      return Null();
    }
    return new JsonElement(JsonBoolean.of(value));
  }

  /**
   * A single JSON number.
   */
  public JsonElement create(Number value) {
    if (value == null) {
      return Null();
    }
    boolean infiniteOrNotANumber = isInfiniteOrNotANumber(value);
    if (quoteInfiniteAndNaN && infiniteOrNotANumber) {
      return create(value.toString());
    } else if (!lenient && infiniteOrNotANumber) {
      throw new IllegalArgumentException(value + " is not a valid number value as per JSON specification. Use #lenient to allow it");
    } else if (quoteLargeNumbers && isTooBigForJs(value)) {
      return create(value.toString());
    }

    return new JsonElement(JsonNumber.of(value.toString()));
  }

  /**
   * A single JSON number, created from a String. This value is not parsed.
   *
   * @param numericValue the number as a string
   * @return a single JSON number
   */
  public JsonElement createNumberFromString(String numericValue) {
    if (numericValue == null) {
      return Null();
    }
    return new JsonElement(JsonNumber.of(numericValue));
  }

  /**
   * The null value.
   */
  public JsonElement Null() {
    return Holder.NULL_ELEMENT;
  }

  private boolean isInfiniteOrNotANumber(Number value) {
    if (value instanceof Double || value instanceof Float || value instanceof DoubleAdder) {
      double doubleValue = value.doubleValue();
      return doubleValue == Double.POSITIVE_INFINITY || doubleValue == Double.NEGATIVE_INFINITY || doubleValue == Double.NaN;
    }
    return false;
  }
}
