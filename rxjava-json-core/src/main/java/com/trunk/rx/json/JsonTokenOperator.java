package com.trunk.rx.json;

import com.trunk.rx.json.impl.JsonParser;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;

/**
 * Lift an {@link Observable} of String fragments to an
 * Observable of {@link JsonToken}s.
 */
public class JsonTokenOperator implements Operator<JsonTokenEvent, Character> {

  private final boolean lenient;

  /**
   * Configure this operator to be strict in what it accepts. Only a single
   * valid JSON document as specified by <a
   * href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
   */
  public static JsonTokenOperator strict() {
    return new JsonTokenOperator(false);
  }

  /**
   * Configure this operator to be liberal in what it accepts. Setting the
   * parser to lenient causes it to ignore the following syntax errors:
   *
   * <ul>
   *   <li>Multiple documents will emitted. Each document will be followed by a
   *       {@link com.trunk.rx.json.token.JsonDocumentEnd} token.
   *   <li>Streams that start with the <a href="#nonexecuteprefix">non-execute
   *       prefix</a>, <code>")]}'\n"</code>.
   *   <li>Streams that include multiple top-level values. With strict parsing,
   *       each stream must contain exactly one top-level value.
   *   <li>Top-level values of any type. With strict parsing, the top-level
   *       value must be an object or an array.
   *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
   *       Double#isInfinite() infinities}.
   *   <li>End of line comments starting with {@code //} or {@code #} and
   *       ending with a newline character.
   *   <li>C-style comments starting with {@code /*} and ending with
   *       {@code *}{@code /}. Such comments may not be nested.
   *   <li>Names that are unquoted or {@code 'single quoted'}.
   *   <li>Strings that are unquoted or {@code 'single quoted'}.
   *   <li>Array elements separated by {@code ;} instead of {@code ,}.
   *   <li>Unnecessary array separators. These are interpreted as if null
   *       was the omitted value.
   *   <li>Names and values separated by {@code =} or {@code =>} instead of
   *       {@code :}.
   *   <li>Name/value pairs separated by {@code ;} instead of {@code ,}.
   * </ul>
   */
  public static JsonTokenOperator lenient() {
    return new JsonTokenOperator(true);
  }

  private JsonTokenOperator(boolean lenient) {
    this.lenient = lenient;
  }

  @Override
  public Subscriber<? super Character> call(Subscriber<? super JsonTokenEvent> s) {
    Subscriber<? super JsonTokenEvent> downstream = new SerializedSubscriber<>(s);
    Subscriber<Character> upstream = new JsonParser(downstream, lenient);
    downstream.add(upstream);
    return upstream;
  }

}
