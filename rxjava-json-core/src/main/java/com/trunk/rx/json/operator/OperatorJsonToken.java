package com.trunk.rx.json.operator;

import java.util.concurrent.atomic.AtomicLong;

import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.impl.JsonParser;
import com.trunk.rx.json.token.JsonToken;

import rx.Observable;
import rx.Observable.Operator;
import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BackpressureUtils;
import rx.observers.SerializedSubscriber;

/**
 * Lift an {@link Observable} of String fragments to an
 * Observable of {@link JsonToken}s.
 */
public class OperatorJsonToken implements Operator<JsonTokenEvent, Character> {

  private final boolean lenient;

  /**
   * Configure this operator to be strict in what it accepts. Only a single
   * valid JSON document as specified by <a
   * href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
   */
  public OperatorJsonToken strict() {
    return new OperatorJsonToken(false);
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
  public OperatorJsonToken lenient() {
    return new OperatorJsonToken(true);
  }

  public OperatorJsonToken() {
    this(false);
  }

  public OperatorJsonToken(boolean lenient) {
    this.lenient = lenient;
  }

  @Override
  public Subscriber<? super Character> call(Subscriber<? super JsonTokenEvent> s) {
    Subscriber<? super JsonTokenEvent> downstream = new SerializedSubscriber<>(s);
    JsonParser upstream = new JsonParser(lenient);
    downstream.add(upstream);
    downstream.setProducer(new ParserProducer(upstream, downstream));
    return upstream;
  }

  private class ParserProducer implements Producer {
    final JsonParser upstream;
    final Subscriber<? super JsonTokenEvent> downstream;

    AtomicLong currentRequest = new AtomicLong(0);
    AtomicLong backlog = new AtomicLong(0);

    private ParserProducer(JsonParser upstream,
                           Subscriber<? super JsonTokenEvent> downstream) {
      this.upstream = upstream;
      this.downstream = downstream;
      upstream.reenterProducer(() -> request(0));
    }

    @Override
    public void request(long n) {
      if (n < 0) {
        throw new IllegalArgumentException();
      }
      if (BackpressureUtils.getAndAddRequest(currentRequest, n) != 0) {
        return;
      }
      long r = BackpressureUtils.addCap(backlog.get(), n);
      backlog.set(r);
      currentRequest.set(r);
      if (r == 0) {
        return;
      }
      if (upstream.completed() && upstream.isEmpty()) {
        complete();
        return;
      }
      if (!upstream.started()) {
        backlog.set(r);
        currentRequest.set(0);
        return;
      }
      // loop so that additional requests are processed in sequence
      for (;;) {
        if (downstream.isUnsubscribed()) {
          return;
        }
        int e = 0;

        if (upstream.isEmpty()) {
          upstream.requestMore();
        }
        if (upstream.isEmpty()) {
          if (upstream.completed() && !downstream.isUnsubscribed()) {
            complete();
            return;
          }
          backlog.set(r);
          currentRequest.set(0);
          return;
        }
        while (r > 0 && !upstream.isEmpty()) {
          downstream.onNext(upstream.poll());
          if (downstream.isUnsubscribed()) {
            return;
          }

          ++e;
          --r;
        }
        if (upstream.isEmpty() && upstream.completed() && !downstream.isUnsubscribed()) {
          complete();
          return;
        }
        // check for more requests
        r = currentRequest.addAndGet(-e);
        backlog.addAndGet(-e);
        if (r == 0) {
          return;
        }
      }
    }

    private void complete() {
      Throwable t = upstream.error();
      if (t != null) {
        downstream.onError(t);
      } else {
        downstream.onCompleted();
      }
      downstream.unsubscribe();
      upstream.unsubscribe();
    }
  }
}
