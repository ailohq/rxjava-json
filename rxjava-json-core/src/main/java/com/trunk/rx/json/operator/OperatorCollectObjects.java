package com.trunk.rx.json.operator;

import com.trunk.rx.json.JsonObjectEvent;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.JsonTokenEvent;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonDocumentEnd;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.internal.operators.BackpressureUtils;
import rx.observers.SerializedSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class OperatorCollectObjects implements rx.Observable.Operator<JsonObjectEvent, com.trunk.rx.json.JsonPathEvent> {

  @Override
  public Subscriber<? super JsonPathEvent> call(Subscriber<? super JsonObjectEvent> s) {
    Subscriber<? super JsonObjectEvent> downstream = new SerializedSubscriber<>(s);
    PathEventSubscriber upstream = new PathEventSubscriber();
    downstream.add(upstream);
    downstream.setProducer(new JsonObjectProducer(upstream, downstream));
    return upstream;
  }

  private class PathEventSubscriber extends Subscriber<JsonPathEvent> {
    AtomicBoolean completed = new AtomicBoolean(false);
    AtomicBoolean started = new AtomicBoolean(false);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Queue<JsonPathEvent> buffer = new ConcurrentLinkedDeque<>();
    Action0 reenterProducer = () -> {};

    PathEventSubscriber() {
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
    public void onNext(JsonPathEvent s) {
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

  private class JsonObjectProducer implements Producer {
    private final PathEventSubscriber upstream;
    private final Subscriber<? super JsonObjectEvent> downstream;

    AtomicLong currentRequest = new AtomicLong(0);
    AtomicLong backlog = new AtomicLong(0);

    JsonPath currentPath = null;
    Collection<JsonTokenEvent> tokenBuffer = new ArrayList<>();

    JsonObjectProducer(PathEventSubscriber upstream, Subscriber<? super JsonObjectEvent> downstream) {
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
        int e = 0;

        if (upstream.buffer.isEmpty()) {
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
        while (r > 0 && !upstream.buffer.isEmpty()) {
          JsonPathEvent pathEvent = upstream.buffer.poll();
          if (isDocumentEnd(pathEvent)) {
            if (!tokenBuffer.isEmpty()) {
              if (emit(null)) {
                ++e;
                --r;
              }
            }
          } else {
            if (newObject(pathEvent)) {
              if (emit(pathEvent.getMatchedPathFragment())) {
                ++e;
                --r;
              }
            }
            tokenBuffer.add(pathEvent.getTokenEvent());
          }
          if (downstream.isUnsubscribed()) {
            return;
          }

          if (tokenBuffer.isEmpty()) {
            if (upstream.completed() && !downstream.isUnsubscribed()) {
              complete();
              return;
            }
            break;
          }
        }
        // check for more requests
        r = currentRequest.addAndGet(-e);
        backlog.addAndGet(-e);
        if (r == 0) {
          return;
        }
      }
    }

    private boolean emit(JsonPath newPath) {
      boolean result = false;
      if (!tokenBuffer.isEmpty()) {
        downstream.onNext(new JsonObjectEvent(currentPath, tokenBuffer));
        tokenBuffer = new ArrayList<>();
        result = true;
      }
      currentPath = newPath;
      return result;
    }

    private boolean newObject(JsonPathEvent pathEvent) {
      return !pathEvent.getMatchedPathFragment().equals(currentPath);
    }

    private boolean isDocumentEnd(JsonPathEvent pathEvent) {
      return pathEvent.getTokenEvent().getToken() == JsonDocumentEnd.instance();
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
