package com.trunk.rx.json.gson.operator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.trunk.rx.json.JsonPathEvent;
import com.trunk.rx.json.exception.MalformedJsonException;
import com.trunk.rx.json.gson.GsonPathEvent;
import com.trunk.rx.json.gson.transformer.TransformerRxJsonGson;
import com.trunk.rx.json.path.JsonPath;
import com.trunk.rx.json.token.JsonBoolean;
import com.trunk.rx.json.token.JsonToken;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.internal.operators.BackpressureUtils;
import rx.observers.SerializedSubscriber;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class OperatorJsonGson implements Observable.Operator<GsonPathEvent, JsonPathEvent> {

  private static final class Holder {
    private static final Gson GSON = TransformerRxJsonGson.Holder.DEFAULT_GSON;
  }

  @Override
  public Subscriber<? super JsonPathEvent> call(Subscriber<? super GsonPathEvent> s) {
    Subscriber<? super GsonPathEvent> downstream = new SerializedSubscriber<>(s);
    PathEventSubscriber upstream = new PathEventSubscriber();
    downstream.add(upstream);
    downstream.setProducer(new GsonProducer(upstream, downstream));
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

  private class GsonProducer implements Producer {
    private final PathEventSubscriber upstream;
    private final Subscriber<? super GsonPathEvent> downstream;

    AtomicLong currentRequest = new AtomicLong(0);
    AtomicLong backlog = new AtomicLong(0);

    String lastName = null;
    JsonElement[] stack = new JsonElement[32];
    int stackSize = 0;

    public GsonProducer(PathEventSubscriber upstream, Subscriber<? super GsonPathEvent> downstream) {
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
          if (consumeAndEmit()) {
            ++e;
            --r;
          }
          if (downstream.isUnsubscribed()) {
            return;
          }

          if (upstream.buffer.isEmpty()) {
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

    private boolean consumeAndEmit() {
      JsonPathEvent event = upstream.buffer.poll();
      JsonToken token = event.getTokenEvent().getToken();
      if (token.isDocumentEnd()) {
        if (stackSize != 0) {
          error("Unexpected document end");
        }
        return false;
      }
      JsonPath path = event.getMatchedPathFragment();
      JsonElement topOfStack = peek();
      if (token.isArrayStart()) {
        if (topOfStack == null) {
          push(new JsonArray());
        } else if (topOfStack.isJsonArray()) {
          JsonArray a = new JsonArray();
          topOfStack.getAsJsonArray().add(a);
          push(a);
        } else if (topOfStack.isJsonObject() && lastName != null) {
          JsonArray a = new JsonArray();
          topOfStack.getAsJsonObject().add(lastName, a);
          lastName = null;
          push(a);
        } else {
          error("Unexpected array start", event);
        }

      } else if (token.isArrayEnd()) {
        if (topOfStack != null && topOfStack.isJsonArray()) {
          return popAndEmitIfObjectComplete(path);
        } else {
          error("Unexpected array end", event);
        }

      } else if (token.isBoolean()) {
        return handlePrimitive(event, new JsonPrimitive(token == JsonBoolean.True()));

      } else if (token.isName()) {
        if (topOfStack != null && topOfStack.isJsonObject() && lastName == null) {
          lastName = token.value();
        } else {
          error("Unexpected name", event);
        }

      } else if (token.isNull()) {
        return handlePrimitive(event, JsonNull.INSTANCE);

      } else if (token.isNumber()) {
        return handlePrimitive(event, new JsonPrimitive(asNumber(token.value())));

      } else if (token.isObjectStart()) {
        if (topOfStack == null) {
          push(new JsonObject());
        } else if (topOfStack.isJsonArray()) {
          JsonObject o = new JsonObject();
          topOfStack.getAsJsonArray().add(o);
          push(o);
        } else if (topOfStack.isJsonObject() && lastName != null) {
          JsonObject o = new JsonObject();
          topOfStack.getAsJsonObject().add(lastName, o);
          lastName = null;
          push(o);
        } else {
          error("Unexpected object start", event);
        }

      } else if (token.isObjectEnd()) {
        if (topOfStack != null && topOfStack.isJsonObject() && lastName == null) {
          return popAndEmitIfObjectComplete(path);
        } else {
          error("Unexpected object end", event);
        }

      } else if (token.isString()) {
        return handlePrimitive(event, new JsonPrimitive(token.value()));

      } else {
        error("Unknown token " + token.toString(), event);
      }
      return false;
    }

    private Number asNumber(String value) {
      return Holder.GSON.fromJson(value, Number.class);
    }

    private boolean handlePrimitive(JsonPathEvent event, JsonElement value) {
      JsonElement topOfStack = peek();
      if (topOfStack == null) {
        return emit(event.getMatchedPathFragment(), value);
      } else if (topOfStack.isJsonArray()) {
        topOfStack.getAsJsonArray().add(value);
      } else if (topOfStack.isJsonObject() && lastName != null) {
        topOfStack.getAsJsonObject().add(lastName, value);
        lastName = null;
      } else {
        error("Unexpected primitive", event);
      }
      return false;
    }

    private void error(String msg, JsonPathEvent event) {
      error(msg + " at " + event.getTokenEvent().getJsonPath());
    }

    private void error(String msg) {
      upstream.onError(new MalformedJsonException(msg));
    }

    private void push(JsonElement newTop) {
      if (stackSize == stack.length) {
        JsonElement[] newStack = new JsonElement[stackSize * 2];
        System.arraycopy(stack, 0, newStack, 0, stackSize);
        stack = newStack;
      }
      stack[stackSize++] = newTop;
    }
    private boolean popAndEmitIfObjectComplete(JsonPath path) {
      stackSize -= 1;
      if (stackSize == 0) {
        return emit(path, stack[0]);
      }
      return false;
    }

    private boolean emit(JsonPath path, JsonElement element) {
      downstream.onNext(new GsonPathEvent(path, element));
      return true;
    }

    private JsonElement peek() {
      return stackSize > 0 ? stack[stackSize-1] : null;
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
