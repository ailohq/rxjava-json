package com.trunk.rx.character.operator;

import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BackpressureUtils;

public class OnSubscribeStringToChar implements Observable.OnSubscribe<Character> {
  private final String source;
  public OnSubscribeStringToChar(String source) {
    this.source = source;
  }

  @Override
  public void call(Subscriber<? super Character> subscriber) {
    if (source.isEmpty()) {
      subscriber.onCompleted();
      return;
    }
    subscriber.setProducer(new CharacterProducer(source, subscriber));
  }

  private static class CharacterProducer implements Producer {
    private final String source;
    private final Subscriber<? super Character> subscriber;
    int index = 0;
    private AtomicLong requested = new AtomicLong(0);

    private CharacterProducer(String source, Subscriber<? super Character> subscriber) {
      this.source = source;
      this.subscriber = subscriber;
    }

    @Override
    public void request(long n) {
      if (n < 0) {
        throw new IllegalArgumentException();
      }
      if (n == 0) {
        return;
      }
      if (BackpressureUtils.getAndAddRequest(requested, n) != 0) {
        return;
      }
      long r = n;
      // loop so that additional requests are processed in sequence
      for (;;) {
        if (subscriber.isUnsubscribed()) {
          return;
        }
        int i = index;
        int e = 0;
        while (r > 0 && i < source.length()) {
          subscriber.onNext(source.charAt(i));
          if (subscriber.isUnsubscribed()) {
            return;
          }

          ++i;
          ++e;
          --r;
          if (i == source.length()) {
            subscriber.onCompleted();
            return;
          }
        }
        index = i;
        // check for more requests
        r = requested.addAndGet(-e);
        if (r == 0) {
          return;
        }
      }
    }
  }
}
