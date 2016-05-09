package com.trunk.rx.character.operator;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.internal.operators.BackpressureUtils;
import rx.observers.SerializedSubscriber;

public class OperatorStringToChar implements Observable.Operator<Character, String> {
  @Override
  public Subscriber<? super String> call(Subscriber<? super Character> s) {
    Subscriber<? super Character> downstream = new SerializedSubscriber<>(s);
    CharSubscriber upstream = new CharSubscriber();
    downstream.add(upstream);
    downstream.setProducer(new CharProducer(upstream, downstream));
    return upstream;
  }

  private final class CharSubscriber extends Subscriber<String> {
    AtomicBoolean completed = new AtomicBoolean(false);
    AtomicBoolean started = new AtomicBoolean(false);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Queue<String> buffer = new ConcurrentLinkedDeque<>();
    Action0 reenterProducer = () -> {};

    CharSubscriber() {
      request(0);
    }

    @Override
    public void setProducer(Producer p) {
      super.setProducer(p);
      started.set(true);
      reenterProducer.call();
    }

    @Override
    public void onCompleted() {
      completed.set(true);
      reenterProducer.call();
    }

    @Override
    public void onError(Throwable e) {
      error.set(e);
      completed.set(true);
      reenterProducer.call();
    }

    @Override
    public void onNext(String s) {
      buffer.add(s);
      started.set(true);
      reenterProducer.call();
    }

    void reenterProducer(Action0 f) {
      reenterProducer = f;
    }

    void requestMore() {
      request(1);
    }

    boolean completed() {
      return completed.get();
    }

    boolean started() {
      return started.get();
    }
  }

  private final class CharProducer implements Producer {
    private final CharSubscriber upstream;
    final Subscriber<? super Character> downstream;

    int index = 0;
    AtomicLong currentRequest = new AtomicLong(0);
    AtomicLong backlog = new AtomicLong(0);

    CharProducer(CharSubscriber upstream, Subscriber<? super Character> downstream) {
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
      if (upstream.completed() && upstream.buffer.isEmpty()) {
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
        int i = index;
        int e = 0;

        if (upstream.buffer.isEmpty()) {
          upstream.requestMore();
        }
        while (!upstream.buffer.isEmpty() && upstream.buffer.peek().isEmpty()) {
          upstream.buffer.poll();
          upstream.requestMore();
        }
        if (upstream.buffer.isEmpty()) {
          if (upstream.completed() && !downstream.isUnsubscribed()) {
            complete();
            return;
          }
          backlog.set(r);
          currentRequest.set(0);
          return;
        }
        String s = upstream.buffer.peek();
        while (r > 0 && i < s.length()) {
          downstream.onNext(s.charAt(i));
          if (downstream.isUnsubscribed()) {
            return;
          }

          ++i;
          ++e;
          --r;
          if (i == s.length()) {
            if (upstream.completed() && !downstream.isUnsubscribed()) {
              complete();
              return;
            }
            upstream.buffer.poll();
            i = 0;
            break;
          }
        }
        index = i;
        // check for more requests
        r = currentRequest.addAndGet(-e);
        backlog.addAndGet(-e);
        if (r == 0) {
          return;
        }
      }
    }

    private void complete() {
      Throwable t = upstream.error.get();
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
